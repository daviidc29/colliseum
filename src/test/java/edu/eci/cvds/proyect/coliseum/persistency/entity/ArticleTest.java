package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {

    private Article emptyArticle;
    private Article fullArticle1;
    private Article fullArticle2;

    @BeforeEach
    void setUp() {
        emptyArticle = new Article();
        fullArticle1 = new Article(1, "Balon", "Disponible", "Balon de futbol", "/images/balon.png");
        fullArticle2 = new Article(1, "Balon", "Disponible", "Balon de futbol", "/images/balon.png");
    }

    @Test
    void testNoArgsConstructor() {
        assertNotNull(emptyArticle);
        assertNull(emptyArticle.getId());
        assertNull(emptyArticle.getName());
        assertNull(emptyArticle.getArticleStatus());
        assertNull(emptyArticle.getDescription());
        assertNull(emptyArticle.getImageUrl());
    }

    @Test
    void testAllArgsConstructor() {
        assertNotNull(fullArticle1);
        assertEquals(1, fullArticle1.getId());
        assertEquals("Balon", fullArticle1.getName());
        assertEquals("Disponible", fullArticle1.getArticleStatus());
        assertEquals("Balon de futbol", fullArticle1.getDescription());
        assertEquals("/images/balon.png", fullArticle1.getImageUrl());
    }

    @Test
    void testBuilder() {
        Article articleBuilt = Article.builder()
                .id(2)
                .name("Raqueta")
                .articleStatus("Dañado")
                .description("Raqueta de tenis")
                .imageUrl("/images/raqueta.png")
                .build();

        assertEquals(2, articleBuilt.getId());
        assertEquals("Raqueta", articleBuilt.getName());
        assertEquals("Dañado", articleBuilt.getArticleStatus());
        assertEquals("Raqueta de tenis", articleBuilt.getDescription());
        assertEquals("/images/raqueta.png", articleBuilt.getImageUrl());
    }

    @Test
    void testSettersAndGetters() {
        emptyArticle.setId(10);
        emptyArticle.setName("Pelota");
        emptyArticle.setArticleStatus("Prestado");
        emptyArticle.setDescription("Pelota de basquet");
        emptyArticle.setImageUrl("/images/pelota.png");

        assertEquals(10, emptyArticle.getId());
        assertEquals("Pelota", emptyArticle.getName());
        assertEquals("Prestado", emptyArticle.getArticleStatus());
        assertEquals("Pelota de basquet", emptyArticle.getDescription());
        assertEquals("/images/pelota.png", emptyArticle.getImageUrl());
    }

    @Test
    void testEqualsAndHashCode() {
        // same data
        assertEquals(fullArticle1, fullArticle2);
        assertEquals(fullArticle1.hashCode(), fullArticle2.hashCode());

        // different ID
        Article diffIdArticle = new Article(2, "Balon", "Disponible", "Balon de futbol", "/images/balon.png");
        assertNotEquals(fullArticle1, diffIdArticle);
        assertNotEquals(fullArticle1.hashCode(), diffIdArticle.hashCode());

        // different status
        Article diffStatusArticle = new Article(1, "Balon", "Perdido", "Balon de futbol", "/images/balon.png");
        assertNotEquals(fullArticle1, diffStatusArticle);
        assertNotEquals(fullArticle1.hashCode(), diffStatusArticle.hashCode());
    }

    @Test
    void testCanEqual() {
        assertTrue(fullArticle1.canEqual(fullArticle2));
        assertFalse(fullArticle1.canEqual(new Object()));
    }

    @Test
    void testToString() {
        String articleString = fullArticle1.toString();
        assertTrue(articleString.contains("id=1"));
        assertTrue(articleString.contains("name=Balon"));
        assertTrue(articleString.contains("articleStatus=Disponible"));
        assertTrue(articleString.contains("description=Balon de futbol"));
        assertTrue(articleString.contains("imageUrl=/images/balon.png"));
    }
}