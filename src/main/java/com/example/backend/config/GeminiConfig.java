package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Gemini API 설정 클래스
 */
@Configuration
public class GeminiConfig {

    /**
     * application.properties의 'gemini.api.key' 값을 주입받습니다.
     * (참고: 사용자님이 붙여넣은 코드 기준)
     * 만약 'gemini.apiKey'를 사용하신다면 "${gemini.apiKey}"로 변경하세요.
     */
    @Value("${gemini.api.key}")
    public String apiKey;

    /**
     * application.properties의 'gemini.api.url' 값을 주입받습니다.
     * 키 이름을 정확히 일치시켰습니다 (gemini.apiUrl -> gemini.api.url).
     */
    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent}")
    public String apiUrl;
}