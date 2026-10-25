package com.resumeai.ai_resume_analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private Boolean isGuest;
    private Boolean emailVerified;
    private String guestSessionToken;
    private Integer uploadCount;
    private Integer chatMessageCount;
    private LocalDateTime createdAt;
}
