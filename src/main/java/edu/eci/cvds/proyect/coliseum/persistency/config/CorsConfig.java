package edu.eci.cvds.proyect.coliseum.persistency.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir solicitudes desde el frontend
        config.addAllowedOrigin(frontendUrl);

        // Configurar métodos HTTP permitidos (incluye PATCH para actualizaciones parciales)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Permitir todos los encabezados, crucialmente Authorization para JWT
        config.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept", "Origin",
                "X-Requested-With", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Exponer estos encabezados al cliente
        config.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
                "Authorization"
        ));

        // Permitir credenciales y cachear preflight por 30 minutos
        config.setAllowCredentials(true);
        config.setMaxAge(1800L);

        // Aplicar esta configuración a todas las rutas
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}