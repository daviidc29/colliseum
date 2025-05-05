package edu.eci.cvds.proyect.coliseum.persistency.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import edu.eci.cvds.proyect.coliseum.persistency.dto.ArticleDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private AlertRepository alertRepository;
    private static final String ARTICLE_ID_NULL = "El ID del articulo no puede ser null";
    private static final String ARTICLE_ID_NOT_FOUND = "Articulo no encontrado con ID: ";
    
    public ArticleService(ArticleRepository articleRepository, AlertRepository alertRepository) {
        this.articleRepository = articleRepository;
        this.alertRepository = alertRepository;
    }


    public List<Article> getAll(){
        return articleRepository.findAll();
    }

    public Article getOne(Integer id){
        if (id == null) {
            throw new IllegalArgumentException(ARTICLE_ID_NULL);
        }
        return articleRepository.findById(id).orElseThrow(() -> new RuntimeException(ARTICLE_ID_NOT_FOUND + id));
    }

    public List<Article> getArticlesNames(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Nombre del artículo no puede ser nulo");
        }
        return articleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Nombre del artículo no encontrado: " + name));
    }
    
    public List<Article> getArticlesStatus(String articleStatus) {
        if (articleStatus == null) {
            throw new IllegalArgumentException("Estado del artículo no puede ser nulo");
        }
        return articleRepository.findByArticleStatus(articleStatus)
                .orElseThrow(() -> new RuntimeException("Estado del artículo no encontrado: " + articleStatus));
    }
    

    public Article save(ArticleDto articleDto){
        if (articleDto == null) {
            throw new RuntimeException("El articulo no puede ser nulo");
        }
        Integer id = autoIncrement();
        Article article = new Article(id, articleDto.getName(), articleDto.getArticleStatus());
        checkStockAndAlert(articleDto.getName());
        return articleRepository.save(article);
    }


    public Article update(Integer id, ArticleDto articleDto){
        if (id == null) {
            throw new IllegalArgumentException(ARTICLE_ID_NULL);
        }
        Article article = articleRepository.findById(id).orElseThrow(() -> new RuntimeException(ARTICLE_ID_NOT_FOUND + id));
        article.setName(articleDto.getName());
        article.setArticleStatus(articleDto.getArticleStatus());
        checkStockAndAlert(articleDto.getName());
        return articleRepository.save(article);
    }

    public Article delete(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException(ARTICLE_ID_NULL);
        }
    
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ARTICLE_ID_NOT_FOUND + id));
    
        String articleName = article.getName(); // Guarda el nombre antes de eliminar
    
        articleRepository.delete(article);
    
        long count = articleRepository.countByNameAndArticleStatus(articleName, "disponible");
    
        if (count < 2) {
            Alert alert = new Alert(
                null,
                articleName,
                "Quedan solo " + count + " artículos disponibles del tipo \"" + articleName + "\"",
                LocalDateTime.now()
            );
            alertRepository.save(alert);
        }
    
        return article;
    }
    
    

    private Integer autoIncrement(){
        List<Article> articles= articleRepository.findAll();
        return articles.isEmpty() ? 1 : articles.stream()
                .max(Comparator.comparing(Article::getId))
                .orElseThrow(() -> new RuntimeException("No se pudo determinar el siguiente ID"))
                .getId() + 1;    
    }
    public Optional<List<Article>> findByName(String articleName) {
        return articleRepository.findByName(articleName);
    }

    public Optional<List<Article>> findByArticleStatus(String articleStatus) {
        return articleRepository.findByName(articleStatus);
    }
    private void checkStockAndAlert(String name) {
        long count = articleRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("Nombre del artículo no encontrado: " + name))
            .stream()
            .filter(a -> a.getArticleStatus().equals("Disponible"))
            .count();
    
        if (count < 2) {
            Alert alert = new Alert(null, name, "Quedan " + count + " disponibles", LocalDateTime.now());
            alertRepository.save(alert);
        }

    }
    public long getAvailableCountByName(String name) {
        return articleRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("Nombre del artículo no encontrado: " + name))
            .stream()
            .filter(a -> a.getArticleStatus().equals("Disponible"))
            .count();
    }
    
    
}
