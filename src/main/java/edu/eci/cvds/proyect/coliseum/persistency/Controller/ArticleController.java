package edu.eci.cvds.proyect.coliseum.persistency.Controller;

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
@Tag(name = "Article resource")
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
    @Operation(summary = "Obtener todos los artículos", description = "Recupera una lista de todos los artículos disponibles.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de artículos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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
    @Operation(summary = "Obtener artículo por ID", description = "Recupera un artículo específico utilizando su ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Artículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Artículo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Object> getOne(
        @Parameter(description = "ID del artículo a recuperar", required = true) @PathVariable("id") Integer id) {
        
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
    @Operation(summary = "Buscar artículos por nombre", description = "Recupera artículos que coinciden con el nombre proporcionado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Artículos encontrados"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> getArticlesNames(
        @Parameter(description = "Nombre del artículo a buscar", required = true) @PathVariable("name") String name) {
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
    @Operation(summary = "Buscar artículos por estado", description = "Recupera artículos que coinciden con el estado proporcionado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Artículos encontrados"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> getArticlesStatus(
        @Parameter(description = "Estado del artículo a buscar", required = true) @PathVariable("articleStatus") String articleStatus) {
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

    @PutMapping("/actualizar/{id}")
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

    @DeleteMapping("/eliminar/{id}")
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
    @GetMapping("/disponibles/{name}")
    @Operation(summary = "Contar artículos disponibles por nombre", description = "Devuelve la cantidad de artículos disponibles que coinciden con el nombre proporcionado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conteo obtenido exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> getAvailableCount(
        @Parameter(description = "Nombre del artículo para contar disponibilidad", required = true) @PathVariable("name") String name) {
        try {
            long count = articleService.getAvailableCountByName(name);
            return ResponseEntity.ok(Collections.singletonMap("disponibles", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Error al contar artículos disponibles: " + e.getMessage()));
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
