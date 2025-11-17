package com.example.backend.config;

import com.example.backend.security.CustomUserDetailsService;
import com.example.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;

/**
 * @EnableWebSecurity : Spring Security를 활성화하는 애너테이션, 웹 보안 기능을 적용하려면 필수
 * 선언 시 : WebSecurityConfiguration이 활성화 -> Spring Security FilterChain을 만들기 위한 HTTP 보안 구성 로직 등록 -> 모든 HTTP 요청이 필터 체인을 거치게 됨
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * @Autowired : 스프링 컨테이너에 등록된 Bean을 자동으로 연결(주입)하는 기능
     */
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * @Bean : 개발자가 직접 특정 객체(Bean)를 만들고 스프링 컨테이너에 등록하도록 지시하는 애너테이션
     * "이 메서드의 리턴값을 Bean으로 등록해줘"라는 의미
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager
            (AuthenticationConfiguration authenticationConfiguration) throws
            Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() { // CORS(Cross-Origin-Resource Sharing) 설정 정의
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5174",
                "http://localhost:5173")); // React 앱 주소
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5174",
                "http://localhost:5173",
                "https://myapp.com",           // ← 프로덕션 프론트엔드 도메인
                "https://www.myapp.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .csrf(csrf -> csrf.disable()) // CSRF 공격 방어 기능 비활성화(Stateless한 JWT방식에서는 비활성화)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않는 Stateless 방식
                .authorizeHttpRequests(authz -> authz // URL 경로별로 접근 권한 설정
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/api/auth/**",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/check-in/**").authenticated()
                        .requestMatchers("/api/analytics/**").authenticated()
                        .requestMatchers("/api/routines/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                );
        // JWT 필터 추가
        // HTTP 요청 처리 전에 JwtAuthenticationFilter가 먼저 동작하도록 설정(토큰 검증)
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
