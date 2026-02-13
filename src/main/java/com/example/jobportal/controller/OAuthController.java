package com.example.jobportal.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.jobportal.util.JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.jobportal.repository.UserRepository userRepository;

    @GetMapping("/user")
    public Map<String, Object> getUserInfo(
            @AuthenticationPrincipal OAuth2User principal) {
        return principal.getAttributes(); // user profile info from google
    }

    @GetMapping("/token")
    public String getAccessToken(OAuth2AuthenticationToken authentication) {
        OidcUser user = (OidcUser) authentication.getPrincipal();
        return user.getIdToken().getTokenValue(); // access token from google
    }

    @PostMapping("/exchange-token")
    public Map<String, String> exchangeToken(@RequestBody Map<String, String> body) {
        String code = body.get("code");

        String redirectUri = "http://localhost:5173/oauth-login";

        String tokenUrl = "https://oauth2.googleapis.com/token";

        // Google requires application/x-www-form-urlencoded
        org.springframework.util.LinkedMultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        RestTemplate restTemplate = new RestTemplate();
        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {
        };

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(tokenUrl, HttpMethod.POST,
                new HttpEntity<>(params, headers), typeRef);

        String idToken = (String) response.getBody().get("id_token");

        // Verify id_token with Google to get user info
        String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        ResponseEntity<Map<String, Object>> tokenInfo = restTemplate.exchange(
                tokenInfoUrl, HttpMethod.GET, null, typeRef);
        String email = (String) tokenInfo.getBody().get("email");
        String name = (String) tokenInfo.getBody().get("name");

        // Auto-create user in DB if not exists
        com.example.jobportal.model.User dbUser = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    com.example.jobportal.model.User newUser = new com.example.jobportal.model.User(
                            name != null ? name : email,
                            email,
                            com.example.jobportal.model.User.AuthProvider.GOOGLE,
                            com.example.jobportal.model.User.Role.APPLICANT);
                    return userRepository.save(newUser);
                });

        // Generate app JWT using email
        String appToken = jwtUtil.generateToken(email);

        Map<String, String> result = new HashMap<>();
        result.put("token", appToken);
        result.put("email", email);
        result.put("role", "ROLE_" + dbUser.getRole().name());
        return result;
    }

    @GetMapping("/user-details")
    public Map<String, Object> getUserInfo(@RequestHeader("Authorization") String token) {
        String idToken = token.replace("Bearer ", "");

        // verify the id token with google
        String userInfoEndpoint = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userInfoEndpoint,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> body = response.getBody();
        return body;

    }
}
