package com.furniro.AuthService.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.furniro.AuthService.util.KeyLoader;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] WHITE_LIST_URLS = {
            "/account/login",
            "/account/login-by-username",
            "/account/register",
            "/account/send-otp",
            "/account/confirm-otp",
            "/account/refresh",
            "/account/change-password",
            "/account/confirm/**",
            "/account/active",
            "/error",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/actuator/**"
    };

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
    private String publicKeyLocation;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(

            AuthenticationConfiguration config) throws Exception {
                
        return config.getAuthenticationManager();
    }

    @Bean
    JwtDecoder jwtDecoder() {

        try {

            RSAPublicKey publicKey = KeyLoader.loadPublicKey(publicKeyLocation);

            return NimbusJwtDecoder.withPublicKey(publicKey).build();

        } catch (Exception e) {

            throw new RuntimeException("Failed to load public key for JwtDecoder", e);
        }
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Collection<GrantedAuthority> authorities = new ArrayList<>();

            String role = jwt.getClaimAsString("role");

            if (role != null) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
            return authorities;
        });

        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)

                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(WHITE_LIST_URLS).permitAll()
                        .anyRequest().authenticated())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                (request, response, authException) -> {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                            "Unauthorized - Please login");
                                })
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                            "Forbidden - You don't have permission");
                                }))

                .oauth2ResourceServer(oauth2 -> oauth2

                        .jwt(jwt -> jwt

                                .decoder(jwtDecoder())

                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/account/login",
                        "/account/login-by-username",
                        "/account/register",
                        "/account/send-otp",
                        "/account/confirm-otp",
                        "/account/change-password",
                        "/account/confirm/**",
                        "/account/active",
                        "/error",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/actuator/**"
                );
    }

}
