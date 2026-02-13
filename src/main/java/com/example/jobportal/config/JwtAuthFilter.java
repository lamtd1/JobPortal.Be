package com.example.jobportal.config;

import com.example.jobportal.model.User;
import com.example.jobportal.repository.UserRepository;
import com.example.jobportal.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                // Extract the username, email
                username = jwtUtil.extractEmail(token);
                System.out.println(">>> JWT extracted username/email: " + username);
            } catch (Exception e) {
                System.out.println("Invalid JWT token: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;

            // Try InMemory first (for admin, user accounts)
            try {
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (Exception e) {
                // Not in InMemory — try DB (for OAuth users)
                var dbUser = userRepository.findByEmail(username);
                if (dbUser.isPresent()) {
                    User user = dbUser.get();
                    userDetails = new org.springframework.security.core.userdetails.User(
                            user.getEmail(),
                            "",
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
                    System.out.println(">>> OAuth user found in DB: " + user.getEmail() + ", role: " + user.getRole());
                } else {
                    System.out.println(">>> User NOT found anywhere for: " + username);
                }
            }

            if (userDetails != null && jwtUtil.validateToken(token, userDetails.getUsername())) {
                System.out.println(
                        ">>> User found: " + userDetails.getUsername() + ", roles: " + userDetails.getAuthorities());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);

    }
}