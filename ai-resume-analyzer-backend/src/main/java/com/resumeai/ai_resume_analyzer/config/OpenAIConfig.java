package com.resumeai.ai_resume_analyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    /**
     * Shared ObjectMapper for JSON parsing across services.
     * Spring AI auto-configures the ChatModel and EmbeddingModel beans
     * from application.properties — no manual wiring needed.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}