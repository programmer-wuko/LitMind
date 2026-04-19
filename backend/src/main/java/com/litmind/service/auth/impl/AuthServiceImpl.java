package com.litmind.service.auth.impl;

import com.litmind.common.exception.BusinessException;
import com.litmind.dto.auth.LoginRequest;
import com.litmind.dto.auth.LoginResponse;
import com.litmind.dto.auth.RegisterRequest;
import com.litmind.dto.auth.UserInfoResponse;
import com.litmind.model.entity.User;
import com.litmind.repository.UserRepository;
import com.litmind.service.auth.AuthService;
import com.litmind.service.auth.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER") // 默cd认角色
                .build();
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(400, "用户名已存在");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(400, "邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());

        User savedUser = userRepository.save(user);

        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generateToken(loadUserByUsername(savedUser.getUsername()), savedUser.getId()));
        response.setUsername(savedUser.getUsername());
        response.setUserId(savedUser.getId());
        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            // 直接验证用户名和密码
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
            
            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(401, "用户名或密码错误");
            }
            
            // 加载用户详情
            UserDetails userDetails = loadUserByUsername(user.getUsername());
            
            // 生成token
            String token = jwtService.generateToken(userDetails, user.getId());

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUsername(user.getUsername());
            response.setUserId(user.getId());
            return response;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            throw new BusinessException(401, "用户名或密码错误");
        }
    }

    @Override
    public UserInfoResponse getCurrentUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未认证");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        return response;
    }
}
