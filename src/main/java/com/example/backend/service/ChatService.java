package com.example.backend.service;

import com.example.backend.config.GeminiConfig;
import com.example.backend.dto.ChatResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 채팅 서비스 클래스
 * Gemini API와 REST 통신하여 AI 응답 처리
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final GeminiConfig geminiConfig;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public ChatService(GeminiConfig geminiConfig) {
        this.geminiConfig = geminiConfig;
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
    public ChatResponse chat(String userMessage) {
        try {
            logger.info("사용자 메시지 수신: {}", userMessage);

            // 1. API 요청 생성
            String requestBody = buildRequestBody(userMessage);
            logger.info("요청 바디 생성 완료");

            // 2. Gemini API 호출
            String responseText = callGeminiApi(requestBody);

            logger.info("Gemini 응답 수신 완료");

            // 3. ChatResponse 객체 생성
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setMessage(responseText);
            chatResponse.setTimestamp(getCurrentTimestamp());

            return chatResponse;

        } catch (Exception e) {
            logger.error("Gemini API 호출 중 오류 발생", e);

            // 에러 응답 생성
            ChatResponse errorResponse = new ChatResponse();
            // 오류 메시지에 예외 상세 정보를 포함하여 디버깅에 도움을 줍니다.
            errorResponse.setMessage("죄송합니다. 현재 서비스를 이용할 수 없습니다. 오류: " + e.getMessage());
            errorResponse.setTimestamp(getCurrentTimestamp());

            return errorResponse;
        }
    }

    /**
     * Gemini API 요청 바디 생성
     */
    private String buildRequestBody(String message) {
        try {
            // Gemini API JSON 형식: { "contents": [ { "parts": [ { "text": "..." } ] } ] }
            JsonObject contents = new JsonObject();
            JsonObject parts = new JsonObject();

            parts.addProperty("text", message);

            com.google.gson.JsonArray partsArray = new com.google.gson.JsonArray();
            partsArray.add(parts);

            contents.add("parts", partsArray);

            com.google.gson.JsonArray contentsArray = new com.google.gson.JsonArray();
            contentsArray.add(contents);

            JsonObject requestJson = new JsonObject();
            requestJson.add("contents", contentsArray);

            return requestJson.toString();
        } catch (Exception e) {
            logger.error("요청 바디 생성 오류", e);
            throw new RuntimeException("요청 바디 생성 실패", e);
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