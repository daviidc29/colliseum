package edu.eci.cvds.proyect.coliseum.persistency.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Modulo Prestamos articulos de coliseo")
                        .version("1.0")
                        .description("API para el módulo de préstamos de articulos del coliseo"));
    }
}