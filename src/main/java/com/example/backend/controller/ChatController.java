package com.example.backend.controller;

import com.example.backend.dto.ChatRequest;
import com.example.backend.dto.ChatResponse;
import com.example.backend.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import org.springframework.security.core.Authentication;

/**
 * 채팅 API 컨트롤러
 * 클라이언트 요청을 받아 ChatService에 전달하고 응답 반환
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS
})
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    /**
     * 생성자 - ChatService 주입
     */
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestBody ChatRequest request, Authentication authentication) {

        logger.info("채팅 요청 수신 (Conv ID: {})", request.getConversationId());

        String email = authentication.getName();
        if(email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 유효성 검사 - 메시지가 비어있는지 확인
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            logger.warn("빈 메시지 요청됨");
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("메시지 내용을 입력해주세요.");
            errorResponse.setTimestamp(getCurrentTimestamp());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }

        try {
            // ChatService에서 응답 받기
            ChatResponse response = chatService.chat(email, request.getMessage(), request.getConversationId());

            logger.info("채팅 응답 전송 (Conv ID: {})", response.getConversationId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("요청 처리 중 서버 오류 발생", e);

            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("서버 내부 오류가 발생했습니다.");
            errorResponse.setTimestamp(getCurrentTimestamp());

            // 예상치 못한 서버 오류 (500 Internal Server Error)
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * API 헬스 체크
     *
     * GET /api/chat/health
     *
     * @return 200 OK 및 상태 메시지
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {

        logger.info("헬스 체크 요청");

        HealthResponse response = new HealthResponse(
                "OK",
                "Chat API is running!",
                getCurrentTimestamp()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 환영 메시지
     */
    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {

        logger.info("환영 요청");

        String welcomeMessage = "Gemini Chat API에 오신 것을 환영합니다! " +
                "POST /api/chat/send로 메시지를 보내주세요.";

        return ResponseEntity.ok(welcomeMessage);
    }

    /**
     * 현재 시간을 문자열로 반환
     */
    private String getCurrentTimestamp() {
        return java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }

    /**
     * 헬스 체크 응답 내부 클래스
     */
    public static class HealthResponse {
        public String status;
        public String message;
        public String timestamp;

        public HealthResponse(String status, String message, String timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}