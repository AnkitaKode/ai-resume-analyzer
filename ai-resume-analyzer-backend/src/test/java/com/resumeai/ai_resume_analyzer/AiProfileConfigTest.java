package com.resumeai.ai_resume_analyzer;

import com.resumeai.ai_resume_analyzer.config.AiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiProfileConfigTest {

    @Test
    void testLocalFeatureEmbeddingModelGeneratesVectors() {
        AiConfig.LocalFeatureEmbeddingModel model = new AiConfig.LocalFeatureEmbeddingModel();

        assertEquals(384, model.dimensions());

        float[] v1 = model.embed("Java Spring Boot Developer with PostgreSQL");
        assertNotNull(v1);
        assertEquals(384, v1.length);

        // Verify non-zero values exist
        boolean hasNonZero = false;
        for (float f : v1) {
            if (f != 0.0f) {
                hasNonZero = true;
                break;
            }
        }
        assertTrue(hasNonZero, "Vector should have non-zero features");
    }

    @Test
    void testLocalFeatureEmbeddingModelBatchCall() {
        AiConfig.LocalFeatureEmbeddingModel model = new AiConfig.LocalFeatureEmbeddingModel();

        EmbeddingRequest request = new EmbeddingRequest(List.of("Resume text 1", "Resume text 2"), null);
        EmbeddingResponse response = model.call(request);

        assertNotNull(response);
        assertEquals(2, response.getResults().size());
        assertEquals(384, response.getResults().get(0).getOutput().length);
        assertEquals(384, response.getResults().get(1).getOutput().length);
    }

    @Test
    void testLocalFeatureEmbeddingModelHandlesEmptyAndNullGracefully() {
        AiConfig.LocalFeatureEmbeddingModel model = new AiConfig.LocalFeatureEmbeddingModel();

        float[] empty = model.embed("");
        assertNotNull(empty);
        assertEquals(384, empty.length);

        float[] nullVec = model.embed((String) null);
        assertNotNull(nullVec);
        assertEquals(384, nullVec.length);
    }
}
