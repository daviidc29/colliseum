package edu.eci.cvds.proyect.coliseum.persistency.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS para permitir localhost (React) y una URL definida como variable de entorno
        String allowedOrigin = System.getenv("ALLOWED_ORIGIN");  // Variable de entorno para la URL adicional
        String frontendUrl = "";

        registry.addMapping("/**") // Aplicar CORS a todos los endpoints
                .allowedOrigins("http://localhost:3000", frontendUrl, allowedOrigin) // Permitir localhost:3000 (React) y la URL adicional
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Permitir cualquier cabecera
                .allowCredentials(true);
    }
}