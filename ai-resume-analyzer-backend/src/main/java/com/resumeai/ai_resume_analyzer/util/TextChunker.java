package com.resumeai.ai_resume_analyzer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TextChunker {

    private static final int CHUNK_SIZE    = 300;  // reduced from 500
    private static final int CHUNK_OVERLAP = 50;   // reduced from 100
    private static final int MAX_CHUNKS    = 30;   // safety cap

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        // Collapse whitespace to reduce memory footprint
        String cleaned = text.replaceAll("\\s+", " ").trim();

        // Hard cap — resumes don't need more than 15k chars
        if (cleaned.length() > 15000) {
            cleaned = cleaned.substring(0, 15000);
            log.warn("Resume text truncated to 15000 characters to prevent heap overflow");
        }

        int start = 0;
        while (start < cleaned.length() && chunks.size() < MAX_CHUNKS) {
            int end = Math.min(start + CHUNK_SIZE, cleaned.length());

            // Break at sentence boundary where possible
            if (end < cleaned.length()) {
                int periodIdx  = cleaned.lastIndexOf('.', end);
                int newlineIdx = cleaned.lastIndexOf('\n', end);
                int boundary   = Math.max(periodIdx, newlineIdx);
                if (boundary > start + CHUNK_OVERLAP) {
                    end = boundary + 1;
                }
            }

            String chunk = cleaned.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            start = end - CHUNK_OVERLAP;
            if (start < 0) start = 0;
            if (start >= cleaned.length()) break;
        }

        log.info("Generated {} chunks from {} characters", chunks.size(), cleaned.length());
        return chunks;
    }
}