package com.example.backend.service;

import com.example.backend.config.GeminiConfig;
import com.example.backend.dto.ChatResponse;
import com.example.backend.model.ChatConversation;
import com.example.backend.model.ChatMessage;
import com.example.backend.model.User;
import com.example.backend.model.UserAnalysis;
import com.example.backend.repository.ChatConversationRepository;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.UserAnalysisRepository;
import com.example.backend.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final GeminiConfig geminiConfig;
    private final OkHttpClient httpClient;
    private final Gson gson;

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserAnalysisRepository userAnalysisRepository;
    private final AnalysisService analysisService;
    private final ChatConversationRepository  chatConversationRepository;

    public ChatService(GeminiConfig geminiConfig,
                       UserRepository userRepository,
                       ChatMessageRepository chatMessageRepository,
                       UserAnalysisRepository userAnalysisRepository,
                       AnalysisService analysisService,
                       ChatConversationRepository chatConversationRepository) {
        this.geminiConfig = geminiConfig;
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userAnalysisRepository = userAnalysisRepository;
        this.analysisService = analysisService;
        this.chatConversationRepository = chatConversationRepository;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * 사용자 메시지를 Gemini API에 보내고 응답 받기
     */
    @Transactional
    public ChatResponse chat(String email, String userMessage, Long conversationId) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setTimestamp(getCurrentTimestamp());

        try {
            logger.info("사용자 {} 메시지 수신: {}", email, userMessage);

            // --- 3. 사용자 정보 및 과거 분석/대화 로드 ---
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

            // --- 10. [신규] 대화 세션(Conversation) 로드 또는 생성 ---
            ChatConversation conversation;
            if (conversationId != null) {
                // 기존 대화 ID가 있으면 로드
                conversation = chatConversationRepository.findById(conversationId)
                        .orElseThrow(() -> new RuntimeException("Conversation not found"));
                // (보안 강화) 이 대화가 현재 사용자의 것인지 확인
                if (!conversation.getUser().getId().equals(user.getId())) {
                    throw new SecurityException("User not authorized for this conversation");
                }
            } else {
                // 대화 ID가 없으면 새로 생성
                conversation = new ChatConversation();
                conversation.setUser(user);
                // (임시) 첫 메시지 30자로 제목 생성
                String title = userMessage.length() > 30 ? userMessage.substring(0, 30) + "..." : userMessage;
                conversation.setTitle(title);
                conversation = chatConversationRepository.save(conversation); // DB에 저장하여 ID 획득
            }

            UserAnalysis analysis = userAnalysisRepository.findByUser(user)
                    .orElse(new UserAnalysis(user)); // 없으면 새 분석 객체 준비

            // =================================================================
            // [수정] user -> conversation 으로 변경
            // =================================================================
            List<ChatMessage> history = chatMessageRepository.findTop10ByChatConversationOrderByCreatedAtDesc(conversation);
            Collections.reverse(history); // 시간순(오래된->최신)으로 뒤집기

            // --- 4. 맞춤형 프롬프트 생성 ---
            String personalizedPrompt = buildPersonalizedPrompt(analysis, history, userMessage);

            // 5. API 요청 바디 생성 (Gemini가 대화 맥락을 이해하도록 수정)
            String requestBody = buildRequestBodyWithContext(history, personalizedPrompt);
            logger.info("맞춤형 요청 바디 생성 완료");

            // 6. Gemini API 호출
            String aiResponseText = callGeminiApi(requestBody);
            logger.info("Gemini JSON 응답 수신: {}", aiResponseText);

            // ✨ [신규] MalformedJsonException 해결을 위한 JSON 정제 로직
            String cleanedJsonText = aiResponseText;
            int jsonStart = cleanedJsonText.indexOf("{");
            int jsonEnd = cleanedJsonText.lastIndexOf("}");

            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                // 첫 '{'와 마지막 '}' 사이의 텍스트만 추출합니다.
                cleanedJsonText = cleanedJsonText.substring(jsonStart, jsonEnd + 1);
            } else {
                // 유효한 JSON을 찾지 못한 경우 (오류 유발을 위해 원본 유지)
                logger.error("Gemini 응답에서 유효한 JSON 객체를 찾지 못했습니다: {}", aiResponseText);
            }
            // ✨ 정제된 텍스트 로깅
            logger.info("정제된 JSON 텍스트: {}", cleanedJsonText);

            // [수정] JSON 파싱 (54번 답변에서 추가됨)
            JsonObject aiResponseJson = JsonParser.parseString(cleanedJsonText).getAsJsonObject();
            String aiReply = aiResponseJson.get("reply").getAsString();
            String emotion = aiResponseJson.get("emotion").getAsString();
            String stressCause = aiResponseJson.get("stressCause").getAsString();

            // --- 14. [수정] 대화 내용 DB에 저장 (Conversation에 연결) ---
            chatMessageRepository.save(new ChatMessage(
                    conversation,
                    ChatMessage.MessageRole.USER,
                    userMessage
            ));
            chatMessageRepository.save(new ChatMessage(
                    conversation,
                    ChatMessage.MessageRole.ASSISTANT,
                    aiReply
            ));

            // --- 15. [수정] 비동기 분석 서비스 호출 (Conversation ID 전달) ---
            analysisService.analyzeConversationAsync(user.getId(), conversation.getId());

            // --- 16. [수정] 프론트엔드로 보낼 최종 응답 객체 구성 ---
            chatResponse.setMessage(aiReply);
            chatResponse.setEmotion(emotion);
            chatResponse.setStressCause(stressCause);
            chatResponse.setRecommendedContents(new ArrayList<>()); // TODO
            chatResponse.setConversationId(conversation.getId()); // 17. 대화 ID 포함

            return chatResponse;
        } catch (Exception e) {
            logger.error("Gemini API 호출 중 오류 발생", e);
            chatResponse.setMessage("죄송합니다. 현재 서비스를 이용할 수 없습니다. 오류: " + e.getMessage());
            return chatResponse;
        }
    }

    /**
     * [신규] 맞춤형 응답을 위한 프롬프트 생성
     */
    private String buildPersonalizedPrompt(UserAnalysis analysis, List<ChatMessage> history, String newMessage) {
        StringBuilder sb = new StringBuilder();

        // 시스템 명령어: AI의 역할을 정의 (공감 및 위로)
        sb.append("너는 매우 공감 능력이 뛰어나고 다정한 친구이자 심리 상담사야.\n");
        sb.append("사용자의 기분을 파악하고, 위로와 지지를 보내주는 것이 너의 역할이야. " +
                "절대로 사용자를 비난하거나 판단하지 마.\n\n");
        sb.append("이제 [사용자의 새 메시지]에 대해 아래 3가지 항목을 포함하는 JSON 객체 '하나만' 응답해줘.\n");
        sb.append("1. 'reply': 사용자의 메시지에 대한 다정한 공감/위로 답변 (줄바꿈이 필요하면 \\n 사용)\n");
        sb.append("2. 'emotion': 사용자의 감정 상태 (예: 'stressed', 'anxious', 'tired', 'neutral', 'positive')\n");
        sb.append("3. 'stressCause': 사용자의 스트레스 원인 (예: '업무 과다', '회의', '마감일', '불안', '기타')\n\n");
        sb.append("중요: 절대로 JSON 객체 외의 다른 텍스트(예: '```json', '답변입니다:')를 포함하지 마.\n\n");


        // (기억 1) 사용자 분석 정보 주입
        if (analysis.getConversationSummary() != null && !analysis.getConversationSummary().isEmpty()) {
            sb.append("[네가 기억해야 할 사용자 정보]\n");
            sb.append(" - 이전 대화 요약: ").append(analysis.getConversationSummary()).append("\n");
            sb.append(" - 사용자의 현재 감정 상태: ").append(analysis.getCurrentSentiment()).append("\n");
            sb.append("이 정보를 바탕으로 사용자를 위로하고 공감해줘.\n\n");
        }

        // (기억 2) 대화 기록은 buildRequestBodyWithContext에서 처리

        // (기억 3) 새 메시지
        sb.append("[사용자의 새 메시지]\n");
        sb.append(newMessage).append("\n\n");
        sb.append("JSON 응답: "); // AI의 JSON 답변을 유도

        return sb.toString();
    }

    /**
     * Gemini API 요청 바디 생성
     */
    /**
     * [수정] Gemini API가 대화 맥락(history)을 이해하도록 요청 바디 수정
     * (기존 buildRequestBody 대체)
     */
    private String buildRequestBodyWithContext(List<ChatMessage> history, String personalizedPrompt) {
        try {
            JsonArray contentsArray = new JsonArray();

            // 1. 대화 기록(history)을 JSON에 추가
            for (ChatMessage msg : history) {
                JsonObject parts = new JsonObject();
                parts.addProperty("text", msg.getContent()); // [수정] .getMessage() -> .getContent()
                JsonArray partsArray = new JsonArray();
                partsArray.add(parts);

                JsonObject contents = new JsonObject();
                contents.add("parts", partsArray);
                // [수정] .getSender() -> .getRole().name()
                contents.addProperty("role", msg.getRole() == ChatMessage.MessageRole.ASSISTANT ? "model" : "user");
                contentsArray.add(contents);
            }

            // 2. 시스템 프롬프트와 새 메시지를 마지막 "user" 턴으로 추가
            // ... (이후 로직은 동일)
            JsonObject lastParts = new JsonObject();
            lastParts.addProperty("text", personalizedPrompt);
            JsonArray lastPartsArray = new JsonArray();
            lastPartsArray.add(lastParts);

            JsonObject lastContents = new JsonObject();
            lastContents.add("parts", lastPartsArray);
            lastContents.addProperty("role", "user");
            contentsArray.add(lastContents);

            // ... (genConfig, requestJson 생성)
            //JsonObject genConfig = new JsonObject();
           // genConfig.addProperty("responseMimeType", "application/json");

            JsonObject requestJson = new JsonObject();
            requestJson.add("contents", contentsArray);
            //requestJson.add("generationConfig", genConfig);

            return requestJson.toString();
        } catch (Exception e) {
            logger.error("JSON 요청 바디 생성 오류", e);
            throw new RuntimeException("JSON 요청 바디 생성 실패", e);
        }
    }

    /**
     * Gemini API 호출 및 응답 처리
     */
    private String callGeminiApi(String requestBody) throws Exception {
        String apiKey = geminiConfig.apiKey;
        String apiUrl = geminiConfig.apiUrl;

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            throw new Exception("API 키가 올바르게 설정되지 않았습니다. application.properties를 확인하세요.");
        }

        // URL에 API 키 추가
        String fullUrl = apiUrl + "?key=" + apiKey;

        logger.info("API 호출 URL: {}", fullUrl);

        RequestBody body = RequestBody.create(
                requestBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // 지수 백오프 (Exponential Backoff)를 사용하여 재시도 로직 구현 (최대 3회)
        int maxRetries = 3;
        long initialDelay = 1000; // 1초

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Response response = httpClient.newCall(request).execute()) {
                logger.info("API 응답 상태 코드: {}", response.code());

                String responseBody = response.body().string();
                logger.debug("API 응답 바디: {}", responseBody);

                if (response.isSuccessful()) {
                    // 성공적으로 응답 받으면 텍스트 추출 후 반환
                    return extractTextFromResponse(responseBody);
                } else if (response.code() == 429 && attempt < maxRetries - 1) {
                    // 429 Too Many Requests일 경우 재시도 (재시도 횟수 확인)
                    long delay = initialDelay * (long) Math.pow(2, attempt);
                    logger.warn("API 호출 실패 (429). {}ms 후 재시도합니다.", delay);
                    TimeUnit.MILLISECONDS.sleep(delay);
                    continue;
                } else {
                    // 기타 실패 (404, 400 등)는 즉시 예외 발생
                    throw new Exception("API 호출 실패: " + response.code() + " - " + responseBody);
                }
            } catch (java.net.SocketTimeoutException e) {
                // 타임아웃 발생 시 재시도
                if (attempt < maxRetries - 1) {
                    long delay = initialDelay * (long) Math.pow(2, attempt);
                    logger.warn("API 호출 타임아웃. {}ms 후 재시도합니다.", delay);
                    TimeUnit.MILLISECONDS.sleep(delay);
                    continue;
                } else {
                    throw e; // 마지막 시도에서 실패하면 예외 던지기
                }
            }
        }

        // 여기에 도달하면 (예: for문 완료) 다시 예외 발생 (실행될 가능성은 낮음)
        throw new Exception("최대 재시도 횟수 초과");
    }

    /**
     * API 응답 JSON에서 텍스트 추출
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            logger.info("응답 파싱 시작");

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            // "error" 필드가 있는지 확인 (API 에러 응답)
            if (jsonResponse.has("error")) {
                return "API 오류 발생: " + jsonResponse.getAsJsonObject("error").get("message").getAsString();
            }

            if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                com.google.gson.JsonObject candidate = jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0)
                        .getAsJsonObject();

                if (candidate.has("content")) {
                    com.google.gson.JsonObject content = candidate.getAsJsonObject("content");

                    if (content.has("parts") && content.getAsJsonArray("parts").size() > 0) {
                        String text = content
                                .getAsJsonArray("parts")
                                .get(0)
                                .getAsJsonObject()
                                .get("text")
                                .getAsString();

                        logger.info("추출된 텍스트: {}", text);
                        return text;
                    }
                }
            }

            // 응답 구조가 예상과 다를 경우 원본 JSON 반환
            logger.warn("응답에서 텍스트를 찾지 못했습니다. 원본 응답: {}", responseBody);
            return "응답을 처리할 수 없습니다. 원본: " + responseBody;
        } catch (Exception e) {
            logger.error("응답 파싱 오류", e);
            return "응답 처리 중 오류 발생: " + e.getMessage();
        }
    }

    /**
     * 현재 시간을 문자열로 반환
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }
}