package com.resumeai.ai_resume_analyzer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.ai_resume_analyzer.security.JwtTokenProvider;
import com.resumeai.ai_resume_analyzer.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        jwtTokenProvider = new JwtTokenProvider(
                "my-super-secret-test-key-minimum-256-bits-long-32-bytes!",
                3600000,   // 1 hour
                86400000,  // 24 hours
                objectMapper
        );
    }

    @Test
    void testGenerateAndValidateToken_Success() {
        UserPrincipal principal = UserPrincipal.builder()
                .id("user-123")
                .email("test@example.com")
                .fullName("Test User")
                .isGuest(false)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        String token = jwtTokenProvider.generateToken(principal, false);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("user-123", jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("ROLE_USER", jwtTokenProvider.getRoleFromToken(token));
        assertFalse(jwtTokenProvider.isGuestFromToken(token));
    }

    @Test
    void testGuestToken() {
        UserPrincipal guestPrincipal = UserPrincipal.builder()
                .id("guest-uuid-456")
                .email("guest@guest.local")
                .fullName("Guest User")
                .isGuest(true)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST")))
                .build();

        String token = jwtTokenProvider.generateToken(guestPrincipal, false);

        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("guest-uuid-456", jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("ROLE_GUEST", jwtTokenProvider.getRoleFromToken(token));
        assertTrue(jwtTokenProvider.isGuestFromToken(token));
    }

    @Test
    void testTamperedToken_FailsValidation() {
        UserPrincipal principal = UserPrincipal.builder()
                .id("user-123")
                .email("test@example.com")
                .fullName("Test User")
                .isGuest(false)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        String token = jwtTokenProvider.generateToken(principal, false);
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + "tampered." + parts[2];

        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    void testInvalidToken_FailsValidation() {
        assertFalse(jwtTokenProvider.validateToken(null));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken("not.a.valid.jwt.token"));
    }
}
