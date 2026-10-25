package com.resumeai.ai_resume_analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String message;
    private boolean devMode;
    private String resetToken;
    private String resetUrl;
}
