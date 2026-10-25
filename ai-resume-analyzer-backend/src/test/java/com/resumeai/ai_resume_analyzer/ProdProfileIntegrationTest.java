package com.resumeai.ai_resume_analyzer;

import com.resumeai.ai_resume_analyzer.config.TestAiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("prod")
@Import(TestAiConfig.class)
class ProdProfileIntegrationTest {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void testProdBeansAreConfigured() {
        assertNotNull(chatModel, "ChatModel must be available in prod profile");
        assertNotNull(embeddingModel, "EmbeddingModel must be available in prod profile");
    }
}
