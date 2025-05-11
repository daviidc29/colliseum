package edu.eci.cvds.proyect.coliseum.persistency.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArticleDtoTest {

    private ArticleDto emptyDto;
    private ArticleDto fullDto1;
    private ArticleDto fullDto2;

    @BeforeEach
    void setUp() {
        emptyDto = new ArticleDto();

        fullDto1 = new ArticleDto(
                "Balon",
                "Disponible",
                "Balon de futbol",
                "/images/balon.png"
        );

        fullDto2 = new ArticleDto(
                "Balon",
                "Disponible",
                "Balon de futbol",
                "/images/balon.png"
        );
    }

    @Test
    void testNoArgsConstructor() {
        assertNotNull(emptyDto);
        assertNull(emptyDto.getName());
        assertNull(emptyDto.getArticleStatus());
        assertNull(emptyDto.getDescription());
        assertNull(emptyDto.getImageUrl());
    }

    @Test
    void testAllArgsConstructor() {
        assertNotNull(fullDto1);
        assertEquals("Balon", fullDto1.getName());
        assertEquals("Disponible", fullDto1.getArticleStatus());
        assertEquals("Balon de futbol", fullDto1.getDescription());
        assertEquals("/images/balon.png", fullDto1.getImageUrl());
    }

    @Test
    void testBuilder() {
        ArticleDto builtDto = ArticleDto.builder()
                .name("Raqueta")
                .articleStatus("Dañado")
                .description("Raqueta de tenis")
                .imageUrl("/images/raqueta.png")
                .build();

        assertNotNull(builtDto);
        assertEquals("Raqueta", builtDto.getName());
        assertEquals("Dañado", builtDto.getArticleStatus());
        assertEquals("Raqueta de tenis", builtDto.getDescription());
        assertEquals("/images/raqueta.png", builtDto.getImageUrl());
    }

    @Test
    void testSettersAndGetters() {
        emptyDto.setName("Pelota");
        emptyDto.setArticleStatus("Prestado");
        emptyDto.setDescription("Pelota de basquet");
        emptyDto.setImageUrl("/images/pelota.png");

        assertEquals("Pelota", emptyDto.getName());
        assertEquals("Prestado", emptyDto.getArticleStatus());
        assertEquals("Pelota de basquet", emptyDto.getDescription());
        assertEquals("/images/pelota.png", emptyDto.getImageUrl());
    }

    @Test
    void testEqualsAndHashCode() {
        // Should be equal
        assertEquals(fullDto1, fullDto2);
        assertEquals(fullDto1.hashCode(), fullDto2.hashCode());

        // Different name
        ArticleDto differentDto = new ArticleDto("Otra", "Disponible", "Balon de futbol", "/images/balon.png");
        assertNotEquals(fullDto1, differentDto);
        assertNotEquals(fullDto1.hashCode(), differentDto.hashCode());
    }

    @Test
    void testCanEqual() {
        // Check canEqual with the same type of object
        assertTrue(fullDto1.canEqual(fullDto2));

        // Check with a different type of object
        assertFalse(fullDto1.canEqual(new Object()));
    }

    @Test
    void testToString() {
        String dtoString = fullDto1.toString();
        assertTrue(dtoString.contains("Balon"));
        assertTrue(dtoString.contains("Disponible"));
    }
}