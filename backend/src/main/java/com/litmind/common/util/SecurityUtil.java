package com.litmind.common.util;

import com.litmind.repository.UserRepository;
import com.litmind.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        String username;
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getPrincipal().toString();
        }

        // 硬编码用户支持（用于开发环境）
        if ("admin".equals(username)) {
            return 1L;
        }

        Optional<User> user = userRepository.findByUsername(username);
        return user.map(User::getId).orElse(null);
    }
}

