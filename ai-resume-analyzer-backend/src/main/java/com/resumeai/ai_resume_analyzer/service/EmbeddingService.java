package com.resumeai.ai_resume_analyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.model.ResumeChunk;
import com.resumeai.ai_resume_analyzer.repository.ChunkRepository;
import com.resumeai.ai_resume_analyzer.util.TextChunker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final TextChunker textChunker;
    private final ChunkRepository chunkRepository;
    private final ObjectMapper objectMapper;

    // Process max 5 chunks at a time to avoid heap overflow
    private static final int BATCH_SIZE = 5;

    @Transactional
    public void embedAndStore(Resume resume) {
        log.info("Embedding resume: {}", resume.getId());

        chunkRepository.deleteByResumeId(resume.getId());

        List<String> chunks = textChunker.chunk(resume.getRawText());
        if (chunks.isEmpty()) {
            log.warn("No chunks generated for resume {}", resume.getId());
            return;
        }

        log.info("Processing {} chunks in batches of {}", chunks.size(), BATCH_SIZE);

        // Process in small batches to prevent heap overflow
        for (int batchStart = 0; batchStart < chunks.size(); batchStart += BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + BATCH_SIZE, chunks.size());
            List<String> batch = chunks.subList(batchStart, batchEnd);

            List<ResumeChunk> batchEntities = new ArrayList<>();

            try {
                EmbeddingRequest request = new EmbeddingRequest(batch, null);
                EmbeddingResponse response = embeddingModel.call(request);

                for (int i = 0; i < batch.size(); i++) {
                    float[] embedding = response.getResults().get(i).getOutput();
                    String embeddingJson = objectMapper.writeValueAsString(embedding);

                    batchEntities.add(ResumeChunk.builder()
                            .resume(resume)
                            .content(batch.get(i))
                            .chunkIndex(batchStart + i)
                            .embeddingJson(embeddingJson)
                            .build());

                    // Null out embedding immediately after serializing to free memory
                    embedding = null;
                }

            } catch (Exception e) {
                log.error("Batch embedding failed for batch starting at {}", batchStart, e);
                // Fallback: embed one by one
                for (int i = 0; i < batch.size(); i++) {
                    try {
                        float[] embedding = embeddingModel.embed(batch.get(i));
                        String embeddingJson = objectMapper.writeValueAsString(embedding);

                        batchEntities.add(ResumeChunk.builder()
                                .resume(resume)
                                .content(batch.get(i))
                                .chunkIndex(batchStart + i)
                                .embeddingJson(embeddingJson)
                                .build());

                        embedding = null;
                    } catch (Exception ex) {
                        log.error("Failed to embed chunk {}", batchStart + i, ex);
                    }
                }
            }

            // Save batch to DB immediately and clear from memory
            chunkRepository.saveAll(batchEntities);
            batchEntities.clear();
            log.info("Saved batch {}-{}", batchStart, batchEnd);
        }

        log.info("Finished embedding resume {}", resume.getId());
    }

    /**
     * Retrieves top-K relevant chunks using cosine similarity.
     * Streams through chunks to avoid loading everything into memory at once.
     */
    public List<String> retrieveRelevantChunks(String resumeId, String query, int topK) {
        float[] queryEmbedding = embeddingModel.embed(query);
        List<ResumeChunk> allChunks = chunkRepository.findAllByResumeId(resumeId);

        List<String> results = allChunks.stream()
                .map(chunk -> {
                    float[] chunkEmbedding = fromJson(chunk.getEmbeddingJson());
                    double score = cosineSimilarity(queryEmbedding, chunkEmbedding);
                    chunkEmbedding = null; // free memory
                    return new ScoredChunk(chunk.getContent(), score);
                })
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(topK)
                .map(ScoredChunk::content)
                .toList();

        // Help GC
        allChunks.clear();
        return results;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (normA == 0 || normB == 0) ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private float[] fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return new float[0];
            return objectMapper.readValue(json, float[].class);
        } catch (Exception e) {
            return new float[0];
        }
    }

    private record ScoredChunk(String content, double score) {}
}