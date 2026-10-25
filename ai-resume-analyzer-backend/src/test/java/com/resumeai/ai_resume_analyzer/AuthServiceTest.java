package com.resumeai.ai_resume_analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.ai_resume_analyzer.dto.*;
import com.resumeai.ai_resume_analyzer.model.PasswordResetToken;
import com.resumeai.ai_resume_analyzer.model.Resume;
import com.resumeai.ai_resume_analyzer.model.User;
import com.resumeai.ai_resume_analyzer.repository.LinkedAccountRepository;
import com.resumeai.ai_resume_analyzer.repository.PasswordResetTokenRepository;
import com.resumeai.ai_resume_analyzer.repository.ResumeRepository;
import com.resumeai.ai_resume_analyzer.repository.UserRepository;
import com.resumeai.ai_resume_analyzer.security.JwtTokenProvider;
import com.resumeai.ai_resume_analyzer.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final Map<String, User> userDb = new HashMap<>();
    private final Map<String, PasswordResetToken> resetTokenDb = new HashMap<>();
    private final Map<String, Resume> resumeDb = new HashMap<>();

    private UserRepository userRepository;
    private LinkedAccountRepository linkedAccountRepository;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    private ResumeRepository resumeRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userDb.clear();
        resetTokenDb.clear();
        resumeDb.clear();

        passwordEncoder = new BCryptPasswordEncoder();
        jwtTokenProvider = new JwtTokenProvider(
                "secret-key-that-is-at-least-32-bytes-long-for-test-jwt!",
                3600000, 86400000, new ObjectMapper());

        // Dynamic proxy for UserRepository
        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        User u = (User) args[0];
                        if (u.getId() == null) u.setId(UUID.randomUUID().toString());
                        userDb.put(u.getId(), u);
                        return u;
                    } else if ("findById".equals(name)) {
                        return Optional.ofNullable(userDb.get(args[0]));
                    } else if ("findByEmailIgnoreCase".equals(name)) {
                        String email = (String) args[0];
                        return userDb.values().stream()
                                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                                .findFirst();
                    } else if ("findByGuestSessionToken".equals(name)) {
                        String token = (String) args[0];
                        return userDb.values().stream()
                                .filter(u -> token.equals(u.getGuestSessionToken()))
                                .findFirst();
                    } else if ("existsByEmailIgnoreCase".equals(name)) {
                        String email = (String) args[0];
                        return userDb.values().stream().anyMatch(u -> email.equalsIgnoreCase(u.getEmail()));
                    }
                    return null;
                }
        );

        // Dynamic proxy for LinkedAccountRepository
        linkedAccountRepository = (LinkedAccountRepository) Proxy.newProxyInstance(
                LinkedAccountRepository.class.getClassLoader(),
                new Class[]{LinkedAccountRepository.class},
                (proxy, method, args) -> args != null && args.length > 0 ? args[0] : null
        );

        // Dynamic proxy for PasswordResetTokenRepository
        passwordResetTokenRepository = (PasswordResetTokenRepository) Proxy.newProxyInstance(
                PasswordResetTokenRepository.class.getClassLoader(),
                new Class[]{PasswordResetTokenRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        PasswordResetToken t = (PasswordResetToken) args[0];
                        if (t.getId() == null) t.setId(UUID.randomUUID().toString());
                        resetTokenDb.put(t.getToken(), t);
                        return t;
                    } else if ("findByToken".equals(name)) {
                        return Optional.ofNullable(resetTokenDb.get(args[0]));
                    } else if ("deleteByUserId".equals(name)) {
                        String uid = (String) args[0];
                        resetTokenDb.values().removeIf(t -> t.getUser() != null && uid.equals(t.getUser().getId()));
                        return null;
                    }
                    return null;
                }
        );

        // Dynamic proxy for ResumeRepository
        resumeRepository = (ResumeRepository) Proxy.newProxyInstance(
                ResumeRepository.class.getClassLoader(),
                new Class[]{ResumeRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("save".equals(name)) {
                        Resume r = (Resume) args[0];
                        if (r.getId() == null) r.setId(UUID.randomUUID().toString());
                        resumeDb.put(r.getId(), r);
                        return r;
                    } else if ("saveAll".equals(name)) {
                        Iterable<Resume> list = (Iterable<Resume>) args[0];
                        list.forEach(r -> resumeDb.put(r.getId(), r));
                        return list;
                    } else if ("findByUserIdOrderByCreatedAtDesc".equals(name)) {
                        String uid = (String) args[0];
                        return resumeDb.values().stream()
                                .filter(r -> r.getUser() != null && uid.equals(r.getUser().getId()))
                                .toList();
                    } else if ("countByUserId".equals(name)) {
                        String uid = (String) args[0];
                        return resumeDb.values().stream()
                                .filter(r -> r.getUser() != null && uid.equals(r.getUser().getId()))
                                .count();
                    }
                    return null;
                }
        );

        authService = new AuthService(
                userRepository,
                linkedAccountRepository,
                passwordResetTokenRepository,
                resumeRepository,
                passwordEncoder,
                jwtTokenProvider
        );
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(authService, "cookieSecure", false);
    }

    @Test
    void testSignUp_Success() {
        SignUpRequest request = SignUpRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("SecurePass123!")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthResponse authResponse = authService.signUp(request, response);

        assertNotNull(authResponse);
        assertNotNull(authResponse.getUser().getId());
        assertEquals("john@example.com", authResponse.getUser().getEmail());
        assertEquals("John Doe", authResponse.getUser().getFullName());
        assertNotNull(response.getHeader("Set-Cookie"));
        assertTrue(response.getHeader("Set-Cookie").contains("AUTH-TOKEN="));
    }

    @Test
    void testSignUp_DuplicateEmailThrowsException() {
        User existing = User.builder()
                .id("u1")
                .fullName("Existing User")
                .email("existing@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build();
        userDb.put("u1", existing);

        SignUpRequest request = SignUpRequest.builder()
                .fullName("John Doe")
                .email("EXISTING@example.com")
                .password("SecurePass123!")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.signUp(request, response));

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testSignUp_WeakPasswordThrowsException() {
        SignUpRequest request = SignUpRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("weak")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.signUp(request, response));

        assertTrue(ex.getMessage().contains("at least 8 characters"));
    }

    @Test
    void testSignIn_Success() {
        User user = User.builder()
                .id("u1")
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role("ROLE_USER")
                .isGuest(false)
                .build();
        userDb.put("u1", user);

        SignInRequest request = SignInRequest.builder()
                .email("john@example.com")
                .password("Password123!")
                .rememberMe(true)
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthResponse authResponse = authService.signIn(request, response);

        assertNotNull(authResponse);
        assertEquals("john@example.com", authResponse.getUser().getEmail());
        assertTrue(response.getHeader("Set-Cookie").contains("AUTH-TOKEN="));
    }

    @Test
    void testSignIn_InvalidPasswordThrowsBadCredentials() {
        User user = User.builder()
                .id("u1")
                .email("john@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .build();
        userDb.put("u1", user);

        SignInRequest request = SignInRequest.builder()
                .email("john@example.com")
                .password("WrongPassword")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(BadCredentialsException.class, () -> authService.signIn(request, response));
    }

    @Test
    void testCreateGuestSession_Success() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthResponse authResponse = authService.createGuestSession(response);

        assertNotNull(authResponse);
        assertTrue(authResponse.getUser().getIsGuest());
        assertEquals("ROLE_GUEST", authResponse.getUser().getRole());
        assertNotNull(authResponse.getUser().getGuestSessionToken());
        assertTrue(response.getHeader("Set-Cookie").contains("AUTH-TOKEN="));
    }

    @Test
    void testForgotPassword_DevMode() {
        User user = User.builder()
                .id("u1")
                .email("john@example.com")
                .build();
        userDb.put("u1", user);

        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("john@example.com")
                .build();

        ForgotPasswordResponse response = authService.forgotPassword(request);

        assertNotNull(response);
        assertTrue(response.isDevMode());
        assertNotNull(response.getResetToken());
        assertTrue(response.getResetUrl().contains(response.getResetToken()));
    }

    @Test
    void testResetPassword_Success() {
        User user = User.builder()
                .id("u1")
                .email("john@example.com")
                .passwordHash(passwordEncoder.encode("OldPassword123!"))
                .build();
        userDb.put("u1", user);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id("tok-1")
                .user(user)
                .token("valid-reset-token")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        resetTokenDb.put("valid-reset-token", resetToken);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("valid-reset-token")
                .newPassword("NewSecurePassword123!")
                .build();

        authService.resetPassword(request);

        assertTrue(passwordEncoder.matches("NewSecurePassword123!", user.getPasswordHash()));
        assertTrue(resetToken.getUsed());
    }

    @Test
    void testTransferGuestData_Success() {
        User guest = User.builder()
                .id("guest-1")
                .isGuest(true)
                .guestSessionToken("token-abc")
                .build();
        userDb.put("guest-1", guest);

        User regUser = User.builder()
                .id("reg-1")
                .isGuest(false)
                .build();
        userDb.put("reg-1", regUser);

        Resume resume1 = Resume.builder().id("res-1").user(guest).build();
        resumeDb.put("res-1", resume1);

        int count = authService.transferGuestData("token-abc", "reg-1");

        assertEquals(1, count);
        assertEquals(regUser, resume1.getUser());
    }
}
