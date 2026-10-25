package com.resumeai.ai_resume_analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UserDto user;
    private String message;
    private String token; // Optional in response body; primary is HTTP-only cookie
}
