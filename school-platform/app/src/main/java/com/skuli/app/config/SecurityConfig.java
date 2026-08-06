package com.skuli.app.config;

import com.skuli.common.security.JwtAuthConverter;
import com.skuli.common.security.TenantContextFilter;
import com.skuli.student.api.StudentDirectory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Resource-server security: validates Keycloak JWTs, maps realm roles to authorities, scopes each
 * request to its tenant, and applies the coarse role rules (translated from the current
 * {@code routeAccessMap}). Fine-grained row-level access lands in Phase 3.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Lazy decoder: defers contacting Keycloak until the first token is validated, so the app boots
     * even when Keycloak (its own separate container) or the realm is not yet reachable.
     */
    @Bean
    public JwtDecoder jwtDecoder(@Value("${keycloak.issuer-uri}") String issuerUri) {
        return new SupplierJwtDecoder(() -> JwtDecoders.fromIssuerLocation(issuerUri));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder jwtDecoder,
                                           ObjectProvider<StudentDirectory> studentDirectory)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // OpenAPI docs + Swagger UI are browsable without a token (read-only API
                        // contract); the endpoints they describe still require auth.
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/subjects/**", "/api/v1/grades/**").hasRole("admin")
                        .requestMatchers(
                                "/api/v1/teachers/**",
                                "/api/v1/students/**",
                                "/api/v1/parents/**",
                                "/api/v1/classes/**",
                                "/api/v1/lessons/**",
                                "/api/v1/assignments/**",
                                "/api/v1/results/**",
                                "/api/v1/attendance/**",
                                "/api/v1/events/**",
                                "/api/v1/announcements/**").hasAnyRole("admin", "teacher")
                        // Reads open to any authenticated user (schedule/info); writes are
                        // restricted per-method with @PreAuthorize on the controller.
                        .requestMatchers("/api/v1/exams/**").authenticated()
                        .requestMatchers("/api/v1/**").hasAnyRole("admin", "teacher", "student", "parent")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(new JwtAuthConverter())))
                .addFilterAfter(new TenantContextFilter(), BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new UserContextFilter(studentDirectory.getIfAvailable()),
                        TenantContextFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
