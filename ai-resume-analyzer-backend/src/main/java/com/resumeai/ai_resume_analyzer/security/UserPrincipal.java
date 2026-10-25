package com.resumeai.ai_resume_analyzer.security;

import com.resumeai.ai_resume_analyzer.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails, OAuth2User, OidcUser {

    private final String id;
    private final String email;
    private final String password;
    private final String fullName;
    private final boolean isGuest;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole() != null ? user.getRole() : "ROLE_USER")
        );

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .fullName(user.getFullName())
                .isGuest(Boolean.TRUE.equals(user.getIsGuest()))
                .authorities(authorities)
                .attributes(Collections.emptyMap())
                .build();
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        return create(user, attributes, null, null);
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        UserPrincipal principal = create(user);
        return UserPrincipal.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .password(principal.getPassword())
                .fullName(principal.getFullName())
                .isGuest(principal.isGuest())
                .authorities(principal.getAuthorities())
                .attributes(attributes)
                .idToken(idToken)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public String getUsername() {
        return email != null ? email : id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return fullName != null ? fullName : (email != null ? email : id);
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }
}
