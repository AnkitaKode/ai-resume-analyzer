package com.resumeai.ai_resume_analyzer.security.oauth2;

import com.resumeai.ai_resume_analyzer.security.CookieUtils;
import com.resumeai.ai_resume_analyzer.security.JwtTokenProvider;
import com.resumeai.ai_resume_analyzer.security.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(frontendUrl);

        UserPrincipal principal;
        Object principalObj = authentication.getPrincipal();

        if (principalObj instanceof UserPrincipal) {
            principal = (UserPrincipal) principalObj;
        } else if (principalObj instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) principalObj;
            OidcIdToken idToken = null;
            OidcUserInfo userInfo = null;
            if (oauthUser instanceof OidcUser) {
                idToken = ((OidcUser) oauthUser).getIdToken();
                userInfo = ((OidcUser) oauthUser).getUserInfo();
            }
            principal = customOAuth2UserService.processOAuth2User(
                    "google",
                    oauthUser.getAttributes(),
                    idToken,
                    userInfo
            );
        } else {
            throw new IllegalStateException("Unexpected principal type: " + (principalObj != null ? principalObj.getClass().getName() : "null"));
        }

        String token = tokenProvider.generateToken(principal, true);

        // Set HTTP-only auth cookie (30 days)
        CookieUtils.addCookie(response, CookieUtils.AUTH_COOKIE_NAME, token, 30 * 24 * 60 * 60, cookieSecure);

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("oauth_success", "true")
                .queryParam("token", token)
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
