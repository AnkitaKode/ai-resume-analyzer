package com.resumeai.ai_resume_analyzer.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UploadResponse {
    private String id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String status;
    private String message;
}

