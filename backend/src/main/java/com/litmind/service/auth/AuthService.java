package com.litmind.service.auth;

import com.litmind.dto.auth.LoginRequest;
import com.litmind.dto.auth.LoginResponse;
import com.litmind.dto.auth.RegisterRequest;
import com.litmind.dto.auth.UserInfoResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    UserInfoResponse getCurrentUserInfo(Authentication authentication);
}
