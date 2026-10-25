package com.resumeai.ai_resume_analyzer.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

@TestConfiguration
public class TestAiConfig {

    @Bean
    @Primary
    public ChatModel mockChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                return new ChatResponse(Collections.emptyList());
            }
        };
    }

    @Bean
    @Primary
    public EmbeddingModel mockEmbeddingModel() {
        return new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                return new EmbeddingResponse(Collections.emptyList());
            }

            @Override
            public float[] embed(String text) {
                return new float[1536];
            }

            @Override
            public float[] embed(Document document) {
                return new float[1536];
            }
        };
    }
}
