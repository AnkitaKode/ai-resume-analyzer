package com.resumeai.ai_resume_analyzer.service;

import com.resumeai.ai_resume_analyzer.dto.*;
import com.resumeai.ai_resume_analyzer.model.AuthProvider;
import com.resumeai.ai_resume_analyzer.model.LinkedAccount;
import com.resumeai.ai_resume_analyzer.model.PasswordResetToken;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.model.User;
import com.resumeai.ai_resume_analyzer.repository.LinkedAccountRepository;
import com.resumeai.ai_resume_analyzer.repository.PasswordResetTokenRepository;
import com.resumeai.ai_resume_analyzer.repository.ResumeRepository;
import com.resumeai.ai_resume_analyzer.repository.UserRepository;
import com.resumeai.ai_resume_analyzer.security.CookieUtils;
import com.resumeai.ai_resume_analyzer.security.JwtTokenProvider;
import com.resumeai.ai_resume_analyzer.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final LinkedAccountRepository linkedAccountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ResumeRepository resumeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    private static final int ONE_DAY_SECONDS = 24 * 60 * 60;
    private static final int THIRTY_DAYS_SECONDS = 30 * 24 * 60 * 60;
    private static final int SEVEN_DAYS_SECONDS = 7 * 24 * 60 * 60;

    @Transactional
    public AuthResponse signUp(SignUpRequest request, HttpServletResponse response) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists. Please sign in instead.");
        }

        // Validate password strength
        validatePasswordStrength(request.getPassword());

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(true) // For local signup, mark true or send verification
                .role("ROLE_USER")
                .isGuest(false)
                .uploadCount(0)
                .chatMessageCount(0)
                .build();

        user = userRepository.save(user);

        LinkedAccount localAccount = LinkedAccount.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerSubject(user.getId())
                .providerEmail(email)
                .build();
        linkedAccountRepository.save(localAccount);

        // If guest session token was provided, transfer guest-owned resumes
        if (StringUtils.hasText(request.getGuestSessionToken())) {
            transferGuestDataInternal(request.getGuestSessionToken(), user);
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String token = tokenProvider.generateToken(principal, false);

        CookieUtils.addCookie(response, CookieUtils.AUTH_COOKIE_NAME, token, ONE_DAY_SECONDS, cookieSecure);

        return AuthResponse.builder()
                .user(mapToDto(user))
                .token(token)
                .message("Account created successfully.")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse signIn(SignInRequest request, HttpServletResponse response) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!StringUtils.hasText(user.getPasswordHash())) {
            throw new BadCredentialsException("This account was created using social login (Google/LinkedIn). Please sign in using your provider.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String token = tokenProvider.generateToken(principal, request.isRememberMe());

        int cookieMaxAge = request.isRememberMe() ? THIRTY_DAYS_SECONDS : ONE_DAY_SECONDS;
        CookieUtils.addCookie(response, CookieUtils.AUTH_COOKIE_NAME, token, cookieMaxAge, cookieSecure);

        return AuthResponse.builder()
                .user(mapToDto(user))
                .token(token)
                .message("Signed in successfully.")
                .build();
    }

    @Transactional
    public AuthResponse createGuestSession(HttpServletResponse response) {
        String guestToken = UUID.randomUUID().toString();
        String shortId = guestToken.substring(0, 8);

        User guest = User.builder()
                .fullName("Guest User (" + shortId + ")")
                .email("guest-" + shortId + "@guest.resumeai.local")
                .role("ROLE_GUEST")
                .isGuest(true)
                .guestSessionToken(guestToken)
                .emailVerified(false)
                .uploadCount(0)
                .chatMessageCount(0)
                .build();

        guest = userRepository.save(guest);

        UserPrincipal principal = UserPrincipal.create(guest);
        String token = tokenProvider.generateToken(principal, false);

        CookieUtils.addCookie(response, CookieUtils.AUTH_COOKIE_NAME, token, SEVEN_DAYS_SECONDS, cookieSecure);

        return AuthResponse.builder()
                .user(mapToDto(guest))
                .token(token)
                .message("Guest session created.")
                .build();
    }

    @Transactional
    public int transferGuestData(String guestSessionToken, String targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUserId));
        return transferGuestDataInternal(guestSessionToken, targetUser);
    }

    private int transferGuestDataInternal(String guestSessionToken, User targetUser) {
        Optional<User> guestUserOpt = userRepository.findByGuestSessionToken(guestSessionToken);
        if (guestUserOpt.isEmpty()) {
            return 0;
        }

        User guestUser = guestUserOpt.get();
        if (guestUser.getId().equals(targetUser.getId())) {
            return 0;
        }

        List<Resume> guestResumes = resumeRepository.findByUserIdOrderByCreatedAtDesc(guestUser.getId());
        for (Resume resume : guestResumes) {
            resume.setUser(targetUser);
        }
        resumeRepository.saveAll(guestResumes);

        log.info("Transferred {} resumes from guest {} to user {}", guestResumes.size(), guestUser.getId(), targetUser.getId());
        return guestResumes.size();
    }

    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        if (userOpt.isEmpty()) {
            // Safe message to avoid email enumeration
            return ForgotPasswordResponse.builder()
                    .message("If an account exists with this email, password reset instructions have been generated.")
                    .devMode(false)
                    .build();
        }

        User user = userOpt.get();
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String resetToken = UUID.randomUUID().toString();
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .user(user)
                .token(resetToken)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(tokenEntity);

        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        log.info("[DEVELOPMENT MODE] Password reset token for {}: {}", email, resetToken);
        log.info("[DEVELOPMENT MODE] Password reset URL: {}", resetUrl);

        return ForgotPasswordResponse.builder()
                .message("Password reset token generated successfully. In development mode, use the provided link to reset your password.")
                .devMode(true)
                .resetToken(resetToken)
                .resetUrl(resetUrl)
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validatePasswordStrength(request.getNewPassword());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token."));

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException("This password reset token has already been used.");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("This password reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password successfully reset for user {}", user.getId());
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUserDto(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return mapToDto(user);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, CookieUtils.AUTH_COOKIE_NAME, cookieSecure);
        SecurityContextHolder.clearContext();
    }

    public UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .isGuest(Boolean.TRUE.equals(user.getIsGuest()))
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .guestSessionToken(user.getGuestSessionToken())
                .uploadCount(user.getUploadCount() != null ? user.getUploadCount() : 0)
                .chatMessageCount(user.getChatMessageCount() != null ? user.getChatMessageCount() : 0)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private void validatePasswordStrength(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigitOrSpecial = password.chars().anyMatch(ch -> Character.isDigit(ch) || !Character.isLetterOrDigit(ch));
        if (!hasLetter || !hasDigitOrSpecial) {
            throw new IllegalArgumentException("Password must contain at least one letter and at least one number or special character.");
        }
    }
}
