package com.resumeai.ai_resume_analyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class AiConfig {

    /**
     * In prod profile: if OPENAI_API_KEY is provided and not empty, use OpenAI embeddings.
     * Otherwise, fallback to an in-memory hashing/term vectorizer model so RAG chunk
     * retrieval and resume scoring function without requiring a paid OpenAI quota.
     */
    @Bean
    @Profile("prod")
    @ConditionalOnMissingBean(EmbeddingModel.class)
    public EmbeddingModel prodEmbeddingModel(
            @Value("${OPENAI_API_KEY:}") String openAiApiKey,
            @Value("${PROD_EMBED_MODEL:text-embedding-3-small}") String embedModel) {

        if (openAiApiKey != null && !openAiApiKey.isBlank() && !openAiApiKey.startsWith("your-")) {
            log.info("Configuring OpenAI EmbeddingModel for prod with model: {}", embedModel);
            var openAiApi = OpenAiApi.builder().apiKey(openAiApiKey).build();
            return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED,
                    OpenAiEmbeddingOptions.builder()
                            .model(embedModel)
                            .build());
        }

        log.info("No OPENAI_API_KEY detected in prod. Using lightweight local feature embedding model for zero-cost RAG.");
        return new LocalFeatureEmbeddingModel();
    }

    /**
     * Resilient 384-dimensional feature-hash embedding model.
     * Deterministic, zero external API calls, zero cost, works completely offline or online.
     */
    public static class LocalFeatureEmbeddingModel implements EmbeddingModel {

        private static final int DIMENSIONS = 384;

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<Embedding> results = new ArrayList<>();
            for (int i = 0; i < request.getInstructions().size(); i++) {
                String text = request.getInstructions().get(i);
                float[] vector = computeVector(text);
                results.add(new Embedding(vector, i));
            }
            return new EmbeddingResponse(results, new EmbeddingResponseMetadata());
        }

        @Override
        public float[] embed(Document document) {
            return computeVector(document.getText());
        }

        @Override
        public float[] embed(String text) {
            return computeVector(text);
        }

        @Override
        public int dimensions() {
            return DIMENSIONS;
        }

        private float[] computeVector(String text) {
            float[] vector = new float[DIMENSIONS];
            if (text == null || text.isBlank()) {
                return vector;
            }

            // Word token frequency hashing
            String[] tokens = text.toLowerCase().split("[^a-zA-Z0-9]+");
            for (String token : tokens) {
                if (token.isBlank()) continue;
                int hash = Math.abs(token.hashCode()) % DIMENSIONS;
                vector[hash] += 1.0f;

                // Character trigram hashing for partial / morphological similarity
                if (token.length() >= 3) {
                    for (int i = 0; i < token.length() - 2; i++) {
                        String trigram = token.substring(i, i + 3);
                        int triHash = Math.abs(trigram.hashCode()) % DIMENSIONS;
                        vector[triHash] += 0.5f;
                    }
                }
            }

            // L2 normalize vector
            double norm = 0.0;
            for (float v : vector) {
                norm += v * v;
            }
            if (norm > 0) {
                float sqrtNorm = (float) Math.sqrt(norm);
                for (int i = 0; i < vector.length; i++) {
                    vector[i] /= sqrtNorm;
                }
            }

            return vector;
        }
    }
}
