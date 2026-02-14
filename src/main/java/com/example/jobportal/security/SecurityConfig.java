package com.example.jobportal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final AuthenticationProvider authenticationProvider;

        // TODO: Uncomment khi enable OAuth2
        // private final JwtService jwtService;
        // private final UserRepository userRepository;

        private static final String[] ALLOW_LIST = {
                        "/api/auth/**",
                        "/error"
                        // TODO: Uncomment khi enable OAuth2
                        // "/oauth2/**",
                        // "/login/oauth2/**"
        };

        // TODO: Uncomment khi enable OAuth2
        // @Bean
        // public OAuth2SuccessHandler oAuth2SuccessHandler() {
        // return new OAuth2SuccessHandler(userRepository, jwtService);
        // }

        @Bean
        public JwtAuthFilter jwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
                return new JwtAuthFilter(jwtService, userDetailsService);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter)
                        throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(req -> req
                                                .requestMatchers(ALLOW_LIST).permitAll()
                                                .requestMatchers("/api/applications/**").hasRole("APPLICANT")
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/jobs/**").hasAnyRole("APPLICANT", "ADMIN")
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType("application/json");
                                                        response.getWriter()
                                                                        .write("{\"error\": \"Unauthorized\", \"message\": \""
                                                                                        + authException.getMessage()
                                                                                        + "\"}");
                                                }))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                // TODO: Uncomment khi enable OAuth2
                // .oauth2Login(oauth -> oauth
                // .successHandler(oAuth2SuccessHandler()));

                return http.build();
        }

}
