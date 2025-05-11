package edu.eci.cvds.proyect.coliseum.persistency.exception;


import org.junit.jupiter.api.Test;

import edu.eci.cvds.proyect.coliseum.persistency.exception.ArticleException;

import static org.junit.jupiter.api.Assertions.*;

class ArticleExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String expectedMessage = "Test message";
        ArticleException exception = new ArticleException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testConstructorWithArticleId() {
        Integer articleId = 42;
        ArticleException exception = new ArticleException(articleId);
        assertEquals("El artículo con ID 42 no está disponible.", exception.getMessage());
    }

    @Test
    void testArticleExceptionArticleNotAvailable() {
        String expectedMessage = "Artículo no disponible";
        ArticleException.ArticleExceptionArticleNotAvailable exception =
                new ArticleException.ArticleExceptionArticleNotAvailable(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }
}