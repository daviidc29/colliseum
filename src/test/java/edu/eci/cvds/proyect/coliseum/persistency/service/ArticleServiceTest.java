package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.dto.ArticleDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllArticlesSuccess() {
        List<Article> articles = Arrays.asList(new Article(1, "Laptop", "Disponible", "Desc", "img.png"));
        when(articleRepository.findAll()).thenReturn(articles);

        List<Article> result = articleService.getAll();
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
    }

    @Test
    void testGetOneArticleSuccess() {
        Article article = new Article(1, "Laptop", "Disponible", "Desc", "img.png");
        when(articleRepository.findById(1)).thenReturn(Optional.of(article));

        Article result = articleService.getOne(1);
        assertEquals(1, result.getId());
    }

    @Test
    void getOneArticleThrowsWhenNullId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> articleService.getOne(null));
        assertEquals("El ID del articulo no puede ser null", exception.getMessage());
    }
    
    @Test
    void testGetOneArticleNotFound() {
        when(articleRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> articleService.getOne(99));
    }

    @Test
    void saveArticleSuccess() {
        ArticleDto dto = new ArticleDto("Balon", "Disponible", "GOLTY\"", "/images/Balon.png");

        Article a = new Article(1, "Balon", "Disponible", "GOLTY\"", "/images/Balon.png");

        when(articleRepository.findAll()).thenReturn(Collections.emptyList());
        when(articleRepository.save(any(Article.class))).thenReturn(a);
        when(articleRepository.findByName("Balon")).thenReturn(Optional.of(Collections.singletonList(a)));

        Article saved = articleService.save(dto);

        assertEquals("Balon", saved.getName());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void saveArticleThrowsWhenNullDto() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> articleService.save(null));
        assertEquals("El artículo no puede ser nulo", exception.getMessage());
    }

    @Test
    void updateArticleSuccess() {
        Article existing = new Article(1, "Balon", "Disponible", "GOLTY", "/images/Balon.png");
        ArticleDto updateDto = new ArticleDto("Balon", "Dañado", "Pantalla rota","/images/Balon.png");

        when(articleRepository.findById(1)).thenReturn(Optional.of(existing));
        when(articleRepository.save(any(Article.class))).thenReturn(existing);
        when(articleRepository.findByName("Balon")).thenReturn(Optional.of(List.of(existing)));

        Article updated = articleService.update(1, updateDto);

        assertEquals("Dañado", updated.getArticleStatus());
        verify(alertRepository, atLeastOnce()).save(any(Alert.class)); // Verifica la alerta por estado dañado
    }

    @Test
    void deleteArticleSuccessAndGeneratesAlert() {
        Article a = new Article(1, "Balon", "Disponible", "GOLTY", "/images/Balon.png");

        when(articleRepository.findById(1)).thenReturn(Optional.of(a));
        when(articleRepository.countByNameAndArticleStatus("Balon", "disponible")).thenReturn(1L);

        Article deleted = articleService.delete(1);

        assertEquals("Balon", deleted.getName());
        verify(alertRepository).save(any(Alert.class));
        verify(articleRepository).delete(a);
        assertEquals(1, deleted.getId());

    }

    @Test
    void getArticlesByNameSuccess() {
        Article a = new Article(1, "Balon", "Disponible", "GOLTY", "/images/Balon.png");
        when(articleRepository.findByName("Balon")).thenReturn(Optional.of(List.of(a)));

        List<Article> result = articleService.getArticlesNames("Balon");

        assertEquals(1, result.size());
    }

    @Test
    void getAvailableCountByNameSuccess() {
        Article a1 = new Article(1, "Raqueta", "Disponible", "", "");
        Article a2 = new Article(2, "Raqueta", "Prestado", "", "");

        when(articleRepository.findByName("Raqueta")).thenReturn(Optional.of(List.of(a1, a2)));

        long count = articleService.getAvailableCountByName("Raqueta");

        assertEquals(1, count);
    }
    @Test
void getOneArticleNotFoundThrows() {
    when(articleRepository.findById(99)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> articleService.getOne(99));

    assertEquals("Articulo no encontrado con ID: 99", exception.getMessage());
    verify(articleRepository).findById(99);
}

@Test
void getArticlesByNameNullThrows() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> articleService.getArticlesNames(null));

    assertEquals("Nombre del artículo no puede ser nulo", exception.getMessage());
    verify(articleRepository, never()).findByName(any());
}

@Test
void getArticlesByNameNotFoundThrows() {
    when(articleRepository.findByName("Inexistente")).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> articleService.getArticlesNames("Inexistente"));

    assertEquals("Nombre del artículo no encontrado: Inexistente", exception.getMessage());
}

@Test
void getArticlesStatusNullThrows() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> articleService.getArticlesStatus(null));

    assertEquals("Estado del artículo no puede ser nulo", exception.getMessage());
    verify(articleRepository, never()).findByArticleStatus(any());
}

@Test
void getArticlesStatusNotFoundThrows() {
    when(articleRepository.findByArticleStatus("Inexistente")).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> articleService.getArticlesStatus("Inexistente"));

    assertEquals("Estado del artículo no encontrado: Inexistente", exception.getMessage());
}

@Test
void updateArticleNotFoundThrows() {
    ArticleDto dto = new ArticleDto("Raqueta", "Disponible", "Nuevo","/images/Raqueta.png");

    when(articleRepository.findById(10)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> articleService.update(10, dto));

    assertEquals("Articulo no encontrado con ID: 10", exception.getMessage());
}

@Test
void updateArticleNullIdThrows() {
    ArticleDto dto = new ArticleDto("Raqueta", "Disponible", "Nuevo","/images/Raqueta.png");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> articleService.update(null, dto));

    assertEquals("El ID del articulo no puede ser null", exception.getMessage());
}

@Test
void deleteArticleNotFoundThrows() {
    when(articleRepository.findById(123)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> articleService.delete(123));

    assertEquals("Articulo no encontrado con ID: 123", exception.getMessage());
}

@Test
void deleteArticleNullIdThrows() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> articleService.delete(null));

    assertEquals("El ID del articulo no puede ser null", exception.getMessage());
}

@Test
void getAvailableCountByNameThrowsIfNameNotFound() {
    when(articleRepository.findByName("Lazo")).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> articleService.getAvailableCountByName("Lazo"));

    assertEquals("Nombre del artículo no encontrado: Lazo", exception.getMessage());
}
    @Test
    void testCheckStockAndAlert() {
        List<Article> articles = List.of(
            new Article(1, "Laptop", "Disponible", "Desc", "img.png"),
            new Article(2, "Laptop", "Prestado", "Desc", "img.png")
        );
        when(articleRepository.findByName("Laptop")).thenReturn(Optional.of(articles));

        articleService.checkStockAndAlert("Laptop");
        verify(alertRepository).save(argThat(alert -> alert.getMessage().contains("Quedan 1 disponibles")));
    }
 
}