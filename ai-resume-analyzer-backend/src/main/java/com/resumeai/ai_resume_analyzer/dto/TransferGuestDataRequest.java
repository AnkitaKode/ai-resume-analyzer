package com.resumeai.ai_resume_analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferGuestDataRequest {

    @NotBlank(message = "Guest session token is required")
    private String guestSessionToken;
}
