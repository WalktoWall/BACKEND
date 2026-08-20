package com.walktowall.backend.global.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. CSRF & FrameOptions 해제 (개발/시연 환경)
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 3. 모든 API 및 이미지 정적 자원 요청 허용 (CORB 문제 방지)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/var/app/uploads/wallarts/**", "/images/**").permitAll()
                        .anyRequest().permitAll()
                )

                // 4. 불필요한 기본 폼 로그인 및 로그아웃 비활성화
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    // CORS 상세 설정 Bean (CORB 차단 해결의 핵심)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 모든 Origin 허용 (allowedOrigins 대신 allowedOriginPatterns 사용)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 2. 허용할 HTTP 메서드 지정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 3. 모든 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));

        // 4. 쿠키 및 인증 헤더 포함 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}