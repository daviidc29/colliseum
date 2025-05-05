package edu.eci.cvds.proyect.coliseum.persistency.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.cvds.proyect.coliseum.persistency.dto.ArticleDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;

@RestController
@RequestMapping("/Article")
public class ArticleController {
     
    private ArticleService articleService;
    private AlertRepository alertRepository;
    private static final String ERROR_KEY = "Error";
    private static final String MESSAGE_KEY = "Message";
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    public ArticleController(ArticleService articleService, AlertRepository alertRepository) {
        this.articleService = articleService;
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public ResponseEntity<List<Article>> getAll() {
        try {
            return new ResponseEntity<>(articleService.getAll(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = (e.getCause() != null) ? e.getCause().toString() : e.getMessage();
            logger.error("Error al obtener los articulos: {}", errorMessage, e);
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }
    @GetMapping("/por-id/{id}")
    public ResponseEntity<Object> getOne(@PathVariable("id") Integer id) {
        try {
            return new ResponseEntity<>(articleService.getOne(id), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Error al obtener el articulo con ID " + id;
            logger.error("{}: {}", errorMessage, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, errorMessage);
            errorResponse.put("details", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }       
    }

    @GetMapping("/por-nombre/{name}")
    public ResponseEntity<?> getArticlesNames(@PathVariable("name") String name) {
        try {
            List<Article> articles = articleService.getArticlesNames(name);
            return new ResponseEntity<>(articles, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Error al obtener el artículo con nombre " + name;
            logger.error("{}: {}", errorMessage, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, errorMessage);
            errorResponse.put("details", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/por-estado/{articleStatus}")
    public ResponseEntity<?> getArticlesStatus(@PathVariable("articleStatus") String articleStatus) {
        try {
            List<Article> articles = articleService.getArticlesStatus(articleStatus);
            return new ResponseEntity<>(articles, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Error al obtener el artículo con estado " + articleStatus;
            logger.error("{}: {}", errorMessage, e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, errorMessage);
            errorResponse.put("details", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping 
    public ResponseEntity<Object> save(@RequestBody ArticleDto articleDto) {
        try {
            Article savedArticle = articleService.save(articleDto);
            return new ResponseEntity<>(savedArticle, HttpStatus.CREATED);
        } catch (Exception e) {
            String errorMessage = (e.getCause() != null) ? e.getCause().toString() : e.getMessage();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al guardar el articulo");
            errorResponse.put(MESSAGE_KEY, errorMessage);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") Integer id, @RequestBody ArticleDto articleDto) {
        try {
            return new ResponseEntity<>(articleService.update(id, articleDto), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = (e.getCause() != null) ? e.getCause().toString() : e.getMessage();
            logger.error("Error al actualizar articulo con ID {}: {}", id, errorMessage, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al actualizar el articulo");
            errorResponse.put(MESSAGE_KEY, errorMessage);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") Integer id) {
        try {
            articleService.delete(id);
            return new ResponseEntity<>(Collections.singletonMap("message", "Articulo eliminado correctamente"), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = (e.getCause() != null) ? e.getCause().toString() : e.getMessage();
            logger.error("Error al eliminar articulo con ID {}: {}", id, errorMessage, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al eliminar el articulo");
            errorResponse.put(MESSAGE_KEY, errorMessage);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
                    
    }
    @GetMapping("/disponibles/{name}")
    public ResponseEntity<?> getAvailableCount(@PathVariable("name") String name) {
        try {
            long count = articleService.getAvailableCountByName(name);
            return ResponseEntity.ok(Collections.singletonMap("disponibles", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Error al contar artículos disponibles: " + e.getMessage()));
        }
    }
    @GetMapping("/alerts")
    public ResponseEntity<?> getAllAlerts() {
        try {
            return new ResponseEntity<>(alertRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("Error", "Error al obtener las alertas");
            errorResponse.put("Message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
