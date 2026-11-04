package com.furbo.security;

public interface JwtService {
    String generateToken(String email);
    boolean validateToken(String token);
    String getEmailFromToken(String token);
}
