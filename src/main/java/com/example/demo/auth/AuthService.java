package com.example.demo.auth;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public Optional<UserResponse> loginVulnerable(LoginRequest request) throws SQLException {
        return authRepository.loginVulnerable(request.getUsername(), request.getPassword());
    }

    public Optional<UserResponse> loginSafe(LoginRequest request) throws SQLException {
        return authRepository.loginSafe(request.getUsername(), request.getPassword());
    }
}

