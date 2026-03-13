package com.resumeai.ai_resume_analyzer.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class ChatRequest {
    private String resumeId;
    private String message;
    private List<ChatHistoryItem> history;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ChatHistoryItem {
        private String role;
        private String content;
    }
}