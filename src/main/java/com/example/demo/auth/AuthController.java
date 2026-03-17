package com.example.demo.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // API 1: vulnerable endpoint requested by user (/login)
    @PostMapping("/login")
    public ResponseEntity<?> loginVulnerable(@RequestBody LoginRequest request) throws SQLException {
        Optional<UserResponse> user = authService.loginVulnerable(request);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password"));
    }

    // API 2: fixed endpoint using PreparedStatement
    @PostMapping("/login-safe")
    public ResponseEntity<?> loginSafe(@RequestBody LoginRequest request) throws SQLException {
        Optional<UserResponse> user = authService.loginSafe(request);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password"));
    }
}

