package com.resumeai.ai_resume_analyzer.controller;

import com.resumeai.ai_resume_analyzer.dto.*;
import com.resumeai.ai_resume_analyzer.security.SecurityUtils;
import com.resumeai.ai_resume_analyzer.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest request,
                                               HttpServletResponse response) {
        AuthResponse authResponse = authService.signUp(request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody SignInRequest request,
                                               HttpServletResponse response) {
        AuthResponse authResponse = authService.signIn(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/guest")
    public ResponseEntity<AuthResponse> continueAsGuest(HttpServletResponse response) {
        AuthResponse authResponse = authService.createGuestSession(response);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        return SecurityUtils.getCurrentUserId()
                .map(userId -> ResponseEntity.ok(authService.getCurrentUserDto(userId)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
                                                      HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResponse response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. You can now sign in with your new password."));
    }

    @PostMapping("/transfer-guest-data")
    public ResponseEntity<Map<String, Object>> transferGuestData(@Valid @RequestBody TransferGuestDataRequest request) {
        return SecurityUtils.getCurrentUserId()
                .map(userId -> {
                    int count = authService.transferGuestData(request.getGuestSessionToken(), userId);
                    return ResponseEntity.ok(Map.of(
                            "transferredCount", (Object) count,
                            "message", count + " resumes transferred to your account."
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
