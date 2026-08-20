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
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    // 1. 서버 정적 이미지 리소스 매핑 설정 (WebMvcConfigurer 구현)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // http://IP:8080/images/14.png  OR  http://IP:8080/var/app/uploads/wallarts/14.png 요청 대응
        registry.addResourceHandler("/images/**", "/var/app/uploads/wallarts/**")
                .addResourceLocations("file:/var/app/uploads/wallarts/");
    }

    // 2. Security 필터 체인 및 권한 허용 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF & FrameOptions 해제
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                // 모든 API 및 정적 이미지 자원 접근 통과 (CORB 및 404/403 해제)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/images/**", "/var/app/uploads/wallarts/**").permitAll()
                        .anyRequest().permitAll()
                )

                // 불필요한 기본 폼 로그인 및 로그아웃 비활성화
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    // 3. CORS 상세 설정 Bean (CORB 에러 차단)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 모든 Origin 허용 (allowedOrigins 대신 allowedOriginPatterns 사용)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 모든 HTTP 메서드 및 헤더 허용
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
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