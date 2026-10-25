package com.resumeai.ai_resume_analyzer.security.oauth2;

import com.resumeai.ai_resume_analyzer.model.AuthProvider;
import com.resumeai.ai_resume_analyzer.model.LinkedAccount;
import com.resumeai.ai_resume_analyzer.model.User;
import com.resumeai.ai_resume_analyzer.repository.LinkedAccountRepository;
import com.resumeai.ai_resume_analyzer.repository.UserRepository;
import com.resumeai.ai_resume_analyzer.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final LinkedAccountRepository linkedAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(
                    userRequest.getClientRegistration().getRegistrationId(),
                    oAuth2User.getAttributes(),
                    null,
                    null
            );
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    @Transactional
    public UserPrincipal processOAuth2User(String registrationId,
                                           Map<String, Object> attributes,
                                           OidcIdToken idToken,
                                           OidcUserInfo userInfo) {
        AuthProvider provider = "google".equalsIgnoreCase(registrationId)
                ? AuthProvider.GOOGLE
                : AuthProvider.LINKEDIN;

        String providerSubject = (String) attributes.get("sub");
        if (!StringUtils.hasText(providerSubject)) {
            providerSubject = (String) attributes.get("id");
        }
        if (!StringUtils.hasText(providerSubject)) {
            throw new IllegalArgumentException("Provider subject ID not found in OAuth2 response");
        }

        String email = (String) attributes.get("email");
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email not found in OAuth2 response from " + registrationId);
        }

        String name = (String) attributes.get("name");
        if (!StringUtils.hasText(name)) {
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");
            if (StringUtils.hasText(givenName)) {
                name = givenName + (StringUtils.hasText(familyName) ? " " + familyName : "");
            } else {
                name = email.split("@")[0];
            }
        }

        // 1. Check if linked account exists by provider + providerSubject
        Optional<LinkedAccount> linkedAccountOpt = linkedAccountRepository
                .findByProviderAndProviderSubject(provider, providerSubject);

        User user;
        if (linkedAccountOpt.isPresent()) {
            user = linkedAccountOpt.get().getUser();
            // Update name or verified flag if needed
            if (StringUtils.hasText(name) && !name.equals(user.getFullName())) {
                user.setFullName(name);
            }
            user.setEmailVerified(true);
            user = userRepository.save(user);
        } else {
            // 2. Check if user exists by email
            Optional<User> existingUserOpt = userRepository.findByEmailIgnoreCase(email);
            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                user.setEmailVerified(true);
            } else {
                // 3. Register brand new user
                user = User.builder()
                        .fullName(name)
                        .email(email.toLowerCase())
                        .emailVerified(true)
                        .role("ROLE_USER")
                        .isGuest(false)
                        .uploadCount(0)
                        .chatMessageCount(0)
                        .build();
            }
            user = userRepository.save(user);

            // Create linked account record
            LinkedAccount newLinkedAccount = LinkedAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerSubject(providerSubject)
                    .providerEmail(email.toLowerCase())
                    .build();
            linkedAccountRepository.save(newLinkedAccount);
        }

        return UserPrincipal.create(user, attributes, idToken, userInfo);
    }
}
