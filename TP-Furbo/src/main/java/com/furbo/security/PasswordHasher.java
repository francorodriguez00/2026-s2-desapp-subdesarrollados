package com.furbo.security;

public interface PasswordHasher {
    String hash(String password);
    boolean verify(String rawPassword, String hashedPassword);
}
