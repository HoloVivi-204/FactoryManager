package com.factory.management.common.config;

import com.factory.management.common.security.CustomJwtDecoder;
import com.factory.management.common.security.JwtAccessDeniedHandler;
import com.factory.management.common.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomJwtDecoder customJwtDecoder;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("scope");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/introspect",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout",
                                "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/factories/**", "/api/v1/departments/**",
                                "/api/v1/production-lines/**", "/api/v1/teams/**",
                                "/api/v1/employees/**", "/api/v1/shifts/**",
                                "/api/v1/machine-types/**", "/api/v1/machines/**",
                                "/api/v1/downtime-reasons/**", "/api/v1/quality-error-types/**",
                                "/api/v1/materials/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/factories/**", "/api/v1/departments/**",
                                "/api/v1/production-lines/**", "/api/v1/teams/**",
                                "/api/v1/employees/**", "/api/v1/shifts/**",
                                "/api/v1/machine-types/**", "/api/v1/machines/**",
                                "/api/v1/downtime-reasons/**", "/api/v1/quality-error-types/**",
                                "/api/v1/materials/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/factories/**", "/api/v1/departments/**",
                                "/api/v1/production-lines/**", "/api/v1/teams/**",
                                "/api/v1/employees/**", "/api/v1/shifts/**",
                                "/api/v1/machine-types/**", "/api/v1/machines/**",
                                "/api/v1/downtime-reasons/**", "/api/v1/quality-error-types/**",
                                "/api/v1/materials/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(authenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .build();
    }

}
