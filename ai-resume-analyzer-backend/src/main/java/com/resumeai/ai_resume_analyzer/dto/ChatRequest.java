package com.resumeai.ai_resume_analyzer.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String resumeId;
    private String message;
    private List<ChatHistoryItem> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryItem {
        private String role;
        private String content;
    }
}
