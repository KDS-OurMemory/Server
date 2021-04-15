package com.kds.ourmemory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // TODO: 불특정 다수의 클라이언트로부터 접속이 요청되는 상황이기 때문에 정확한 사용법을 확인해야 한다.
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("Content-Type")
            .maxAge(3000);
    }
}
