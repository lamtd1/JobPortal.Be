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
                var jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build();
        }

        public AuthenticationResponse login(AuthenticationRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));
                var user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                var jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build();
        }

}
