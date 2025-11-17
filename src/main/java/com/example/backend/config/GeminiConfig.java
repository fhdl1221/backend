package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Gemini API 설정 클래스
 */

/**
 * @Configuration이 붙은 클래스는 스프링이 Bean으로 등록 -> 스프링이 클래스를 생성하면서 @Value 확인하고 Environment에서 같은 키를 찾아 값을 대입
 * 내부 동작 : properties 파일 읽기 -> PropertySourcesPlaceholderConfigurer로 값 등록 -> Bean 생성 -> Reflection 사용해서 @Value 변수에 값 주입
 * 비유 : application.properties는 환경 설정 창고, @Value는 창고에서 꺼내는 기능
 * But, @Configuration은 @Value와는 상관없음 Why? @Configuration의 역할은 Bean을 등록하는 설정 클래스임을 나타내는 것 즉, 클래스 안에 @Bean 메서드가 있다면 반드시 @Configuration 작성
 * @Configuration = 스프링에게 설정 클래스임을 알려주는 역할, 이 클래스 안에서 Bean을 생성해달라고 선언하는 역할
 */
@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    public String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent}")
    public String apiUrl;
}