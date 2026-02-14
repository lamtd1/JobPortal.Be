package com.example.jobportal.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.jobportal.auth.dto.AuthenticationRequest;
import com.example.jobportal.auth.dto.AuthenticationResponse;
import com.example.jobportal.auth.dto.RegisterRequest;
import com.example.jobportal.security.JwtService;
import com.example.jobportal.user.model.AuthProvider;
import com.example.jobportal.user.model.User;
import com.example.jobportal.user.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService {
        private final PasswordEncoder passwordEncoder;
        private final UserRepository userRepository;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        // Nếu register -> tạo user, lưu DB -> trả về token
        public AuthenticationResponse register(RegisterRequest request) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new RuntimeException("Email already exists: " + request.getEmail());
                }

                var user = User.builder()
                                .displayName(request.getDisplayName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .authProvider(AuthProvider.LOCAL)
                                .role(request.getRole())
                                .build();

                userRepository.save(user);
                return buildTokenResponse(user);
        }

        public AuthenticationResponse login(AuthenticationRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));
                var user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                return buildTokenResponse(user);
        }

        // Dùng refresh token để lấy access token mới
        public AuthenticationResponse refreshToken(String refreshToken) {
                String email = jwtService.extractUsername(refreshToken);
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                if (!jwtService.isTokenValid(refreshToken, user)) {
                        throw new RuntimeException("Invalid refresh token");
                }

                // Chỉ trả access token mới, giữ nguyên refresh token
                return AuthenticationResponse.builder()
                                .accessToken(jwtService.generateAccessToken(user))
                                .refreshToken(refreshToken)
                                .build();
        }

        // Helper: tạo cả access + refresh token
        private AuthenticationResponse buildTokenResponse(User user) {
                return AuthenticationResponse.builder()
                                .accessToken(jwtService.generateAccessToken(user))
                                .refreshToken(jwtService.generateRefreshToken(user))
                                .build();
        }

}
