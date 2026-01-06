package com.litmind.controller.auth;

import com.litmind.common.response.ApiResponse;
import com.litmind.dto.auth.LoginRequest;
import com.litmind.dto.auth.LoginResponse;
import com.litmind.dto.auth.RegisterRequest;
import com.litmind.dto.auth.UserInfoResponse;
import com.litmind.service.auth.AuthService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ApiResponse.success("注册成功", response);
        } catch (Exception e) {
            // 异常会被GlobalExceptionHandler处理，这里只是确保日志记录
            throw e;
        }
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUser(Authentication authentication) {
        UserInfoResponse userInfo = authService.getCurrentUserInfo(authentication);
        return ApiResponse.success(userInfo);
    }
}

