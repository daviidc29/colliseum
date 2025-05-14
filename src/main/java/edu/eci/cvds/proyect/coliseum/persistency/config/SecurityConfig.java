package edu.eci.cvds.proyect.coliseum.persistency.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configures the security filter chain for the application.
 * <p>
 * This class sets up the security configurations for the application, including
 * disabling CSRF protection, configuring session management to be stateless, allowing
 * unauthenticated access to Swagger UI, API documentation, and authentication endpoints,
 * restricting access to the "/api/v1.0/prestamos/**" endpoint to users with the "ADMIN" role,
 * requiring authentication for all other requests, disabling HTTP Basic and form-based login,
 * and adding a JWT authentication filter before the UsernamePasswordAuthenticationFilter.
 */
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain for the application.
     *
     * <p>This method sets up the following security configurations:
     * <ul>
     *   <li>Disables CSRF protection.</li>
     *   <li>Configures session management to be stateless.</li>
     *   <li>Allows unauthenticated access to Swagger UI, API documentation, and authentication endpoints.</li>
     *   <li>Restricts access to the "/api/v1.0/prestamos/**" endpoint to users with the "ADMIN" role.</li>
     *   <li>Requires authentication for all other requests.</li>
     *   <li>Disables HTTP Basic and form-based login.</li>
     *   <li>Adds a JWT authentication filter before the UsernamePasswordAuthenticationFilter.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to modify
     * @return the {@link SecurityFilterChain} that defines the security configuration
     * @throws Exception if an error occurs while configuring the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/v1.0/auth/**").permitAll()
                        .requestMatchers("/api/v1.0/prestamos/**").hasRole("Administrativo")
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}