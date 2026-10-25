package com.resumeai.ai_resume_analyzer.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    private final byte[] secretKey;
    private final long expirationMs;
    private final long rememberMeExpirationMs;
    private final ObjectMapper objectMapper;

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String ENCODED_HEADER = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));

    public JwtTokenProvider(
            @Value("${app.jwt.secret:default-secret-key-change-me-in-production-min-32-bytes!}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs, // 24 hours
            @Value("${app.jwt.remember-me-expiration-ms:2592000000}") long rememberMeExpirationMs, // 30 days
            ObjectMapper objectMapper
    ) {
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
        this.rememberMeExpirationMs = rememberMeExpirationMs;
        this.objectMapper = objectMapper;
    }

    public String generateToken(UserPrincipal principal, boolean rememberMe) {
        long now = System.currentTimeMillis();
        long exp = now + (rememberMe ? rememberMeExpirationMs : expirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", principal.getId());
        claims.put("email", principal.getEmail());
        claims.put("name", principal.getFullName());
        claims.put("role", principal.getAuthorities().isEmpty() ? "ROLE_USER"
                : principal.getAuthorities().iterator().next().getAuthority());
        claims.put("isGuest", principal.isGuest());
        claims.put("iat", now / 1000);
        claims.put("exp", exp / 1000);

        try {
            String payloadJson = objectMapper.writeValueAsString(claims);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String dataToSign = ENCODED_HEADER + "." + encodedPayload;
            String signature = sign(dataToSign);

            return dataToSign + "." + signature;
        } catch (Exception e) {
            log.error("Failed to generate JWT", e);
            throw new RuntimeException("Could not generate token", e);
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            String dataToSign = parts[0] + "." + parts[1];
            String expectedSignature = sign(dataToSign);

            if (!MessageDigest.isEqual(
                    parts[2].getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                log.warn("Invalid JWT signature");
                return false;
            }

            JsonNode payload = parsePayload(parts[1]);
            long exp = payload.path("exp").asLong(0);
            long nowInSeconds = System.currentTimeMillis() / 1000;

            if (exp > 0 && nowInSeconds > exp) {
                log.debug("JWT token is expired");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        try {
            JsonNode payload = parsePayload(parts[1]);
            return payload.path("sub").asText(null);
        } catch (Exception e) {
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        try {
            JsonNode payload = parsePayload(parts[1]);
            return payload.path("role").asText("ROLE_USER");
        } catch (Exception e) {
            return "ROLE_USER";
        }
    }

    public boolean isGuestFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;
        try {
            JsonNode payload = parsePayload(parts[1]);
            return payload.path("isGuest").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }

    private JsonNode parsePayload(String encodedPayload) throws Exception {
        byte[] bytes = Base64.getUrlDecoder().decode(encodedPayload);
        return objectMapper.readTree(bytes);
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, HMAC_SHA256);
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }
}
