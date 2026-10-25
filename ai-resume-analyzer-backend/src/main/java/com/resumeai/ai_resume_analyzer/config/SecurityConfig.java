package com.resumeai.ai_resume_analyzer.config;

import com.resumeai.ai_resume_analyzer.security.JwtAuthenticationFilter;
import com.resumeai.ai_resume_analyzer.security.RestAccessDeniedHandler;
import com.resumeai.ai_resume_analyzer.security.RestAuthenticationEntryPoint;
import com.resumeai.ai_resume_analyzer.security.oauth2.CustomOAuth2UserService;
import com.resumeai.ai_resume_analyzer.security.oauth2.CustomOidcUserService;
import com.resumeai.ai_resume_analyzer.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.resumeai.ai_resume_analyzer.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.resumeai.ai_resume_analyzer.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/health",
                                "/error",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                );

        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(authorization -> authorization
                            .baseUri("/oauth2/authorization")
                            .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                    )
                    .redirectionEndpoint(redirection -> redirection
                            .baseUri("/login/oauth2/code/*")
                    )
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                            .oidcUserService(customOidcUserService)
                    )
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            );
        }

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        java.util.Set<String> originSet = new java.util.LinkedHashSet<>();
        originSet.add("http://localhost:3000");
        originSet.add("http://localhost:5173");
        originSet.add("http://127.0.0.1:3000");

        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            for (String origin : allowedOrigins.split(",")) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    if (trimmed.endsWith("/")) {
                        trimmed = trimmed.substring(0, trimmed.length() - 1);
                    }
                    originSet.add(trimmed);
                }
            }
        }
        configuration.setAllowedOrigins(new java.util.ArrayList<>(originSet));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
