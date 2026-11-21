package com.nexa.backend.security;

import com.nexa.backend.entity.User;
import com.nexa.backend.repository.UserRepository;
import com.nexa.common.exception.ErrorCode;
import com.nexa.common.exception.NexaException;
import com.nexa.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName())
                .orElse("ROLE_USER");

        return UserPrincipal.create(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.isEnabled(),
                role
        );
    }
}
