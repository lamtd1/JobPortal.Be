package com.example.jobportal.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.jobportal.auth.dto.AuthenticationRequest;
import com.example.jobportal.auth.dto.AuthenticationResponse;
import com.example.jobportal.auth.dto.RefreshTokenRequest;
import com.example.jobportal.auth.dto.RegisterRequest;
import com.example.jobportal.auth.service.AuthService;
import com.example.jobportal.user.model.Role;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request) {
        // Default role khi register là APPLICANT
        request.setRole(Role.APPLICANT);
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthenticationResponse> registerAdmin(@RequestBody RegisterRequest request) {
        request.setRole(Role.ADMIN);
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Dùng refresh token để lấy access token mới
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }
}
