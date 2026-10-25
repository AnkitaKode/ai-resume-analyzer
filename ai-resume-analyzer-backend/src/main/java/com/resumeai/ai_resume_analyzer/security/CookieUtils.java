package com.resumeai.ai_resume_analyzer.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.util.Optional;

public class CookieUtils {

    public static final String AUTH_COOKIE_NAME = "AUTH-TOKEN";

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean secure) {
        String sameSite = secure ? "None" : "Lax";
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name, boolean secure) {
        String sameSite = secure ? "None" : "Lax";
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
