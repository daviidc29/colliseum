package edu.eci.cvds.proyect.coliseum.persistency.config;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import edu.eci.cvds.proyect.coliseum.persistency.service.ApiClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_CLAIM = "\"role\":";
    private static final String USER_ID_CLAIM = "\"userId\":";

    private final ApiClient apiClient;

    public JwtAuthenticationFilter(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && apiClient.validateToken(jwt)) {
                // Extraer información del token
                String role = extractRoleFromJwt(jwt);
                String userId = extractUserIdFromJwt(jwt);

                // Configurar la autenticación con información adicional
                setAuthenticationContext(request, role, userId);

                // Registrar la actividad para auditoría
                LOGGER.debug("Usuario con ID {} y rol {} accedió a {}",
                        userId, role, request.getRequestURI());
            }
        } catch (Exception ex) {
            LOGGER.error("Error procesando autenticación JWT: {}", ex.getMessage(), ex);
            // No lanzar excepción para permitir que la solicitud continúe al filtro de seguridad
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private String extractRoleFromJwt(String token) {
        try {
            String[] splitToken = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(splitToken[1]));
            if (payload.contains(ROLE_CLAIM)) {
                int startIndex = payload.indexOf(ROLE_CLAIM) + ROLE_CLAIM.length();
                int endIndex = payload.indexOf('"', startIndex + 1);
                return payload.substring(startIndex + 1, endIndex).toUpperCase();
            }
            return "USER"; // Rol predeterminado si no se encuentra
        } catch (Exception ex) {
            LOGGER.error("Error decodificando payload del token", ex);
            return "USER"; // Rol predeterminado en caso de error
        }
    }

    private String extractUserIdFromJwt(String token) {
        try {
            String[] splitToken = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(splitToken[1]));
            if (payload.contains(USER_ID_CLAIM)) {
                int startIndex = payload.indexOf(USER_ID_CLAIM) + USER_ID_CLAIM.length();
                int endIndex = payload.indexOf('"', startIndex + 1);
                return payload.substring(startIndex + 1, endIndex);
            }
        } catch (Exception ex) {
            LOGGER.error("Error extrayendo userId del token", ex);
        }
        return null;
    }

    private void setAuthenticationContext(HttpServletRequest request, String role, String userId) {
        // Crear la autoridad basada en el rol
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(ROLE_PREFIX + role));

        // Usar userId como principal (objeto que representa al usuario autenticado)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // Agregar detalles de la solicitud
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Establecer en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}