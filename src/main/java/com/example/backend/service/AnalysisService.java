package com.example.backend.service;

import com.example.backend.config.GeminiConfig;
import com.example.backend.model.ChatConversation;
import com.example.backend.model.ChatMessage;
import com.example.backend.model.User;
import com.example.backend.model.UserAnalysis;
import com.example.backend.repository.ChatConversationRepository;
import com.example.backend.repository.ChatMessageRepository;
import com.example.backend.repository.UserAnalysisRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.example.backend.repository.UserRepository; // 1. UserRepository 임포트
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 2. 예외 임포트
import org.springframework.transaction.annotation.Transactional; // 3. 트랜잭션 임포트

@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    // ChatService와 동일하게 API 호출을 위한 의존성 주입
    private final GeminiConfig geminiConfig;
    private final OkHttpClient httpClient;
    private final Gson gson;

    private final ChatMessageRepository chatMessageRepository;
    private final UserAnalysisRepository userAnalysisRepository;
    private final UserRepository userRepository; // 4. UserRepository 주입
    private final ChatConversationRepository chatConversationRepository;

    public AnalysisService(GeminiConfig geminiConfig,
                           ChatMessageRepository chatMessageRepository,
                           UserAnalysisRepository userAnalysisRepository,
                           UserRepository userRepository,
                           ChatConversationRepository chatConversationRepository) {
        this.geminiConfig = geminiConfig;
        this.chatMessageRepository = chatMessageRepository;
        this.userAnalysisRepository = userAnalysisRepository;
        this.userRepository = userRepository;
        this.chatConversationRepository = chatConversationRepository;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * [수정] AI가 반환한 텍스트에서 JSON을 추출하는 로직 추가
     */
    @Async
    @Transactional
    public void analyzeConversationAsync(Long userId, Long conversationId) {

        try {
            // 10. [신규] userId로 '관리되는' User 객체를 다시 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

            // [신규] 8. conversationId로 Conversation 객체 조회
            ChatConversation conversation = chatConversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found: " + conversationId));

            logger.info("유저 {} (Conv ID: {}) 비동기 분석 시작", user.getEmail(), conversation.getId());
            // [수정] 9. Conversation 기준으로 대화 내역 로드
            List<ChatMessage> history = chatMessageRepository.findTop10ByChatConversationOrderByCreatedAtDesc(conversation);
            Collections.reverse(history);

            if (history.isEmpty()) {
                logger.info("분석할 대화 내역이 없습니다.");
                return;
            }

            // 2. 분석용 프롬프트 생성
            String analysisPrompt = buildAnalysisPrompt(history);

            // 3. Gemini API 호출 (AI가 일반 텍스트 응답 반환)
            String aiResponseText = callGeminiApiForAnalysis(analysisPrompt);

            // 4. [신규] 반환된 텍스트에서 JSON 부분만 추출
            String jsonString = extractJsonFromString(aiResponseText);

            if (jsonString == null) {
                // AI가 JSON 추출에 실패한 경우
                logger.warn("응답 텍스트에서 JSON을 추출할 수 없습니다. 원본: {}", aiResponseText);
                throw new Exception("응답 텍스트에서 JSON을 추출할 수 없습니다.");
            }

            // 5. 추출된 JSON 문자열을 파싱
            JsonObject result = gson.fromJson(jsonString, JsonObject.class);
            String summary = result.get("summary").getAsString();
            String sentiment = result.get("sentiment").getAsString();

            // [수정] 10. User 기준으로 UserAnalysis 저장
            UserAnalysis analysis = userAnalysisRepository.findByUser(user)
                    .orElse(new UserAnalysis(user));

            analysis.setConversationSummary(summary);
            analysis.setCurrentSentiment(sentiment);
            userAnalysisRepository.save(analysis);

            logger.info("유저 {} 분석 완료. 감정: {}", user.getEmail(), sentiment);

        } catch (Exception e) {
            // 비동기 태스크에서 발생한 모든 예외를 로깅
            logger.error("비동기 분석 실패 (User ID: {}): {}", userId, e.getMessage(), e);
        }
    }

    /**
     * 분석 전용 프롬프트
     */
    private String buildAnalysisPrompt(List<ChatMessage> history) {
        String conversationText = history.stream()
                .map(msg -> msg.getRole().name() + ": " + msg.getContent()) // .getSender() -> .getRole().name()
                .collect(Collectors.joining("\n"));

        return "다음 대화록을 읽고 2가지 작업을 수행한 뒤, 반드시 JSON 객체 문자열 하나만 응답해줘:\n" +
                "1. 'summary': 대화의 핵심 내용을 2줄로 요약해줘.\n" +
                "2. 'sentiment': 대화에서 드러나는 사용자의 주된 감정을 '긍정', '부정', '중립', '복합' 중 하나로 분류해줘.\n\n" +
                "[대화록 시작]\n" +
                conversationText +
                "\n[대화록 끝]\n\n" +
                "중요: 절대로 JSON 객체 외의 다른 텍스트(예: '요약입니다:', '```json')를 포함하지 마. " +
                "오직 {\"summary\": \"...\", \"sentiment\": \"...\"} 형식으로만 응답해.";
    }

    /**
     * [신규] 3. 응답 텍스트에서 JSON 객체 부분만 추출하는 헬퍼 메소드
     */
    private String extractJsonFromString(String text) {
        // AI가 ```json ... ``` 같은 마크다운을 썼을 경우를 대비
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            String json = text.substring(firstBrace, lastBrace + 1);
            logger.info("추출된 JSON 문자열: {}", json);
            return json;
        }

        logger.warn("응답에서 JSON 객체를 찾지 못했습니다: {}", text);
        return null; // 파싱 실패
    }

    /**
     * ChatService의 buildRequestBody와 동일한 로직
     */
    private String buildRequestBody(String message) {
        JsonObject parts = new JsonObject();
        parts.addProperty("text", message);
        com.google.gson.JsonArray partsArray = new com.google.gson.JsonArray();
        partsArray.add(parts);

        JsonObject contents = new JsonObject();
        contents.add("parts", partsArray);
        com.google.gson.JsonArray contentsArray = new com.google.gson.JsonArray();
        contentsArray.add(contents);

        JsonObject requestJson = new JsonObject();
        requestJson.add("contents", contentsArray);
        return requestJson.toString();
    }

    /**
     * [신규] 분석 API 전용 요청 바디 생성 (JSON 모드 활성화)
     * Gemini가 반드시 JSON으로 응답하도록 강제합니다.
     */
    private String buildAnalysisRequestBody(String message) {
        try {
            // 1. Contents (프롬프트)
            JsonObject parts = new JsonObject();
            parts.addProperty("text", message);
            com.google.gson.JsonArray partsArray = new com.google.gson.JsonArray();
            partsArray.add(parts);

            JsonObject contents = new JsonObject();
            contents.add("parts", partsArray);
            com.google.gson.JsonArray contentsArray = new com.google.gson.JsonArray();
            contentsArray.add(contents);

            // 2. Generation Config (JSON 모드)
            JsonObject genConfig = new JsonObject();
            genConfig.addProperty("response_mime_type", "application/json");

            // 3. 최종 요청 JSON
            JsonObject requestJson = new JsonObject();
            requestJson.add("contents", contentsArray);
            requestJson.add("generationConfig", genConfig); // <-- 이 부분이 핵심입니다!

            return requestJson.toString();
        } catch (Exception e) {
            logger.error("분석 요청 바디 생성 오류", e);
            throw new RuntimeException("분석 요청 바디 생성 실패", e);
        }
    }
    /**
     * ChatService의 callGeminiApi, extractTextFromResponse와 거의 동일한 로직
     */
    /**
     * [수정] ChatService의 재시도/오류 처리를 동일하게 적용
     */
    private String callGeminiApiForAnalysis(String prompt) throws Exception {

        // --- 1. [되돌리기] 단순 텍스트 요청 바디 생성 ---
        JsonObject parts = new JsonObject();
        parts.addProperty("text", prompt);
        com.google.gson.JsonArray partsArray = new com.google.gson.JsonArray();
        partsArray.add(parts);

        JsonObject contents = new JsonObject();
        contents.add("parts", partsArray);
        com.google.gson.JsonArray contentsArray = new com.google.gson.JsonArray();
        contentsArray.add(contents);

        JsonObject requestJson = new JsonObject();
        requestJson.add("contents", contentsArray);
        String requestBody = requestJson.toString();
        // --- 1. 되돌리기 끝 ---

        String apiKey = geminiConfig.apiKey;
        String apiUrl = geminiConfig.apiUrl;
        String fullUrl = apiUrl + "?key=" + apiKey;

        RequestBody body = RequestBody.create(
                requestBody,
                MediaType.parse("application/json; charset=utf-8")
        );
        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        int maxRetries = 3;
        long initialDelay = 1000;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try (Response response = httpClient.newCall(request).execute()) {
                logger.info("분석 API 응답 코드: {}", response.code());
                String responseBody = response.body().string();
                logger.debug("분석 API 응답 바디: {}", responseBody);

                if (response.isSuccessful()) {
                    // --- 2. [수정] 텍스트만 추출해서 반환 ---
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    if (jsonResponse.has("candidates")) {
                        return jsonResponse
                                .getAsJsonArray("candidates")
                                .get(0).getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0).getAsJsonObject()
                                .get("text").getAsString(); // AI가 응답한 '일반 텍스트' 반환
                    } else {
                        throw new Exception("API가 성공했으나 'candidates' 필드가 없음: " + responseBody);
                    }
                }
                else if ((response.code() == 429 || response.code() == 503) && attempt < maxRetries - 1) {
                    long delay = initialDelay * (long) Math.pow(2, attempt);
                    logger.warn("분석 API 호출 실패 ({}), {}ms 후 재시도.", response.code(), delay);
                    TimeUnit.MILLISECONDS.sleep(delay);
                    continue; // 재시도
                } else {
                    throw new Exception("분석 API 호출 실패: " + response.code() + " - " + responseBody);
                }
            } catch (java.net.SocketTimeoutException e) {
                if (attempt < maxRetries - 1) {
                    long delay = initialDelay * (long) Math.pow(2, attempt);
                    logger.warn("분석 API 타임아웃. {}ms 후 재시도.", delay);
                    TimeUnit.MILLISECONDS.sleep(delay);
                    continue;
                } else {
                    throw e;
                }
            }
        }
        throw new Exception("분석 API 호출 최대 재시도 횟수 초과");
    }
}