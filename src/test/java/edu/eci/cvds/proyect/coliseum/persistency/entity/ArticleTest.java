package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {

    @Test
    void getIdTest() {
        Article article = new Article(1, "Balon", "Disponible", "Balon GOLTY", "Balon.jpg");
        assertEquals(1, article.getId());
    }

    @Test
    void getNameTest() {
        Article article = new Article(1, "Lazo", "Prestado", "Lazo largo", "Lazo.png");
        assertEquals("Lazo", article.getName());
    }

    @Test
    void getArticleStatusTest() {
        Article article = new Article(1, "Raqueta", "Dañado", "Raqueta profesional", "Raqueta.jpg");
        assertEquals("Dañado", article.getArticleStatus());
    }

    @Test
    void getDescriptionTest() {
        Article article = new Article(1, "Pelota", "Disponible", "Pelota tennis", "Pelota.jpg");
        assertEquals("Pelota tennis", article.getDescription());
    }

    @Test
    void getImageUrlTest() {
        Article article = new Article(1, "balon", "Devuelto", "balon gráfica", "balon.png");
        assertEquals("balon.png", article.getImageUrl());
    }

    @Test
    void setIdTest() {
        Article article = new Article();
        article.setId(10);
        assertEquals(10, article.getId());
    }

    @Test
    void setNameTest() {
        Article article = new Article();
        article.setName("balon");
        assertEquals("balon", article.getName());
    }

    @Test
    void setArticleStatusTest() {
        Article article = new Article();
        article.setArticleStatus("Perdido");
        assertEquals("Perdido", article.getArticleStatus());
    }

    @Test
    void setDescriptionTest() {
        Article article = new Article();
        article.setDescription("balon futbol");
        assertEquals("balon futbol", article.getDescription());
    }

    @Test
    void setImageUrlTest() {
        Article article = new Article();
        article.setImageUrl("balon.jpg");
        assertEquals("balon.jpg", article.getImageUrl());
    }

    @Test
    void builderTest() {
        Article article = Article.builder()
                .id(5)
                .name("Raquetas")
                .articleStatus("RequireMantenimiento")
                .description("Raquetas profesional")
                .imageUrl("micro.jpg")
                .build();

        assertEquals(5, article.getId());
        assertEquals("Raquetas", article.getName());
        assertEquals("RequireMantenimiento", article.getArticleStatus());
        assertEquals("Raquetas profesional", article.getDescription());
        assertEquals("micro.jpg", article.getImageUrl());
    }
}