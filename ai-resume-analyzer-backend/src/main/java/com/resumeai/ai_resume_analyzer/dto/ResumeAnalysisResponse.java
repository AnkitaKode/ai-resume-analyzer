package com.resumeai.ai_resume_analyzer.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {
    private String resumeId;
    private int score;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private List<String> atsKeywords;
    private String summary;
}