package edu.eci.cvds.proyect.coliseum.persistency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


    @Getter
    @Setter
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class ArticleDto {
        @NotBlank(message="El nombre de articulo no puede estar vació")
        @Size(max=500,message="El nombre del articulo no puede tener mas de 500 caracteres")
        private String name;
        @NotBlank(message="El estado del articulo no puede estar vació")
        @Pattern(regexp = "Disponible|Dañado|RequireMantenimiento|Prestado|Devuelto|Perdido", message = "El estado del articulo no es valido")
        private String articleStatus;
        private String description;
        private String imageUrl;
    
}
