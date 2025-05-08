package edu.eci.cvds.proyect.coliseum.persistency.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.cvds.proyect.coliseum.persistency.dto.ArticleDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;

@RestController
@RequestMapping("/Article")
@Slf4j
@Tag(name = "Articles")
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
    @Operation(summary = "Buscar u obtener todos los artículos", description = """
        Si no se proporciona parámetro, devuelve todos los artículos.
        Si es un número, busca por ID.
        Si es un estado válido (Disponible, Dañado, RequireMantenimiento, Prestado, Devuelto, Perdido), busca por estado.
        Si comienza con 'disponibles:', filtra por nombre y estado Disponible.
        De lo contrario, busca por nombre.
        Incluye el total de artículos encontrados.
        """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Artículos obtenidos correctamente"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> getArticles(@RequestParam(value = "q", required = false) String q) {
        try {
            List<Article> result;

            if (q == null || q.isBlank()) {
                result = articleService.getAll();
            } else if (q.matches("\\d+")) {
                Article article = articleService.getOne(Integer.parseInt(q));
                result = (article != null) ? List.of(article) : List.of();
            } else if (q.startsWith("disponibles:")) {
                String nombre = q.substring("disponibles:".length());
                result = articleService.getArticlesNames(nombre).stream()
                        .filter(a -> "Disponible".equalsIgnoreCase(a.getArticleStatus()))
                        .toList();
            } else {
                Set<String> estadosValidos = Set.of("Disponible", "Dañado", "RequireMantenimiento", "Prestado", "Devuelto", "Perdido");
                result = estadosValidos.contains(q)
                        ? articleService.getArticlesStatus(q)
                        : articleService.getArticlesNames(q);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("cantidad", result.size());
            response.put("articulos", result);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error al obtener artículos: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener artículos");
            errorResponse.put("details", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping 
    @Operation(summary = "Crear un nuevo artículo", description = "Agrega un nuevo artículo al sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Artículo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Object> save(
        @Parameter(description = "Detalles del artículo a crear", required = true) @RequestBody ArticleDto articleDto) {
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

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un artículo existente", description = "Actualiza los detalles de un artículo existente utilizando su ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Artículo actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Object> update(
        @Parameter(description = "ID del artículo a actualizar", required = true) @PathVariable("id") Integer id,
        @Parameter(description = "Nuevos detalles del artículo", required = true) @RequestBody ArticleDto articleDto) {
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un artículo", description = "Elimina un artículo existente utilizando su ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Artículo eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Object> delete(
        @Parameter(description = "ID del artículo a eliminar", required = true) @PathVariable("id") Integer id) {
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
   
    @GetMapping("/alerts")
    @Operation(summary = "Obtener todas las alertas", description = "Recupera todas las alertas relacionadas con los artículos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alertas obtenidas exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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
