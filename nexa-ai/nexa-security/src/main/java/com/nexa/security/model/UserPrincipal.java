package com.nexa.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private UUID id;
    private String email;
    private String password;
    private String displayName;
    private boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(UUID id, String email, String password,
                                       String displayName, boolean enabled, String role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        return UserPrincipal.builder()
                .id(id)
                .email(email)
                .password(password)
                .displayName(displayName)
                .enabled(enabled)
                .authorities(authorities)
                .build();
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return enabled; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
