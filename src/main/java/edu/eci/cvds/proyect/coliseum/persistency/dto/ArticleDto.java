package edu.eci.cvds.proyect.coliseum.persistency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class ArticleDto {
        private String name;
        private String articleStatus;
        private String description;
        private String imageUrl;
    
}
