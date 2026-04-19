package com.litmind.service.auth;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

public interface JwtService {

    String extractUsername(String token);

    <T> T extractClaim(String token, Function<io.jsonwebtoken.Claims, T> claimsResolver);

    String generateToken(UserDetails userDetails, Long userId);

    Long extractUserId(String token);

    Boolean isTokenValid(String token, UserDetails userDetails);
}
