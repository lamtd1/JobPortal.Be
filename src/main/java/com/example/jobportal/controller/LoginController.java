package com.example.jobportal.controller;

import com.example.jobportal.util.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
// controller - scope: ko control header, method, expose header
// va phai add thu cong vao tung method
// @CrossOrigin(origins = {"http://localhost:5173"})
public class LoginController {

    private final JwtUtil jwtUtil;

    public LoginController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Map<String, String> login(Principal principal) {
        // fetch from Spring Security
        String username = principal.getName();

        // genereate token
        String token = jwtUtil.generateToken(username);

        // Return as json
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;

    }

    @GetMapping("/details")
    public Map<String, Object> getUserDetails(Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            response.put("username", userDetails.getUsername());
            response.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

        } else {
            response.put("error", "User not authenticated");
        }

        return response;

    }
}