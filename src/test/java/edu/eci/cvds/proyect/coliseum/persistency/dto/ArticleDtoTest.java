package edu.eci.cvds.proyect.coliseum.persistency.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ArticleDtoTest {

    @Test
    void getNameTest() {
        ArticleDto articleDto = new ArticleDto("Balon", "available", "GOLTY Balon", "image-url.jpg");
        assertEquals("Balon", articleDto.getName());
    }

    @Test
    void getArticleStatusTest() {
        ArticleDto articleDto = new ArticleDto("Balon", "reserved", "GOLTY Balon", "image-url.jpg");
        assertEquals("reserved", articleDto.getArticleStatus());
    }

    @Test
    void getDescriptionTest() {
        ArticleDto articleDto = new ArticleDto("Balon", "available", "Balon description", "image-url.jpg");
        assertEquals("Balon description", articleDto.getDescription());
    }

    @Test
    void getImageUrlTest() {
        ArticleDto articleDto = new ArticleDto("Balon", "available", "Balon description", "image.jpg");
        assertEquals("image.jpg", articleDto.getImageUrl());
    }

    @Test
    void setNameTest() {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setName("Monitor");
        assertEquals("Monitor", articleDto.getName());
    }

    @Test
    void setArticleStatusTest() {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setArticleStatus("damaged");
        assertEquals("damaged", articleDto.getArticleStatus());
    }

    @Test
    void setDescriptionTest() {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setDescription("Monitor Full HD");
        assertEquals("Monitor Full HD", articleDto.getDescription());
    }

    @Test
    void setImageUrlTest() {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setImageUrl("new-image.png");
        assertEquals("new-image.png", articleDto.getImageUrl());
    }

    @Test
    void builderTest() {
        ArticleDto articleDto = ArticleDto.builder()
                .name("Mouse")
                .articleStatus("available")
                .description("Wireless mouse")
                .imageUrl("mouse.jpg")
                .build();

        assertEquals("Mouse", articleDto.getName());
        assertEquals("available", articleDto.getArticleStatus());
        assertEquals("Wireless mouse", articleDto.getDescription());
        assertEquals("mouse.jpg", articleDto.getImageUrl());
    } 
}