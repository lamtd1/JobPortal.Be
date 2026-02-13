package com.example.jobportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.jobportal.repository.UserRepository;

import java.util.List;

@Configuration
public class SecurityConfig {

        // @Bean
        // IN-MEMORY user?
        // public UserDetailsService userDetailsService() {
        // UserDetails admin = User.withUsername("admin@example.com")
        // .password("{noop}admin123")
        // .roles("ADMIN")
        // .build();

        // UserDetails applicant = User.withUsername("user@example.com")
        // .password("{noop}user123")
        // .roles("APPLICANT")
        // .build();

        // return new InMemoryUserDetailsManager(admin, applicant);
        // }

        private final UserRepository userRepository;

        public SecurityConfig(UserRepository userRepository) {
                this.userRepository = userRepository;
        }

        @Bean
        UserDetailsService userDetailsService() {
                return username -> userRepository.findByEmail(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
                return http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/oauth/exchange-token", "/api/oauth/user-details")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/jobs/all").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/applications/apply/**")
                                                .hasRole("APPLICANT")
                                                .requestMatchers(HttpMethod.POST, "/api/auth/login").authenticated()
                                                .anyRequest().authenticated()

                                )
                                .exceptionHandling(ex -> ex
                                                .defaultAuthenticationEntryPointFor(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                                request -> request.getRequestURI().startsWith("/api/")))
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                                .httpBasic(Customizer.withDefaults())
                                // OAuth thi ko the chia ROLE duoc nen can default ROLE
                                // .oauth2Login(Customizer.withDefaults())
                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(userRequest -> {
                                                                        var delegate = new DefaultOAuth2UserService();
                                                                        var oauth2User = delegate.loadUser(userRequest);

                                                                        // assign ROLE = APPLICANT to every OAuth User
                                                                        return new DefaultOAuth2User(
                                                                                        List.of(new SimpleGrantedAuthority(
                                                                                                        "ROLE_APPLICANT")),
                                                                                        oauth2User.getAttributes(),
                                                                                        "email");
                                                                })))
                                .build();
        }

        // Config o global level
        @Bean
        UrlBasedCorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("http://localhost:5173"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(List.of("Authorization"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}