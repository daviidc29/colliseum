package edu.eci.cvds.proyect.coliseum.persistency.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import edu.eci.cvds.proyect.coliseum.persistency.entity.ArticleLoanStats;
import edu.eci.cvds.proyect.coliseum.persistency.entity.LoanArticle;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleLoanStatsRepository;
import edu.eci.cvds.proyect.coliseum.persistency.service.ArticleFileGenerationService;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/Article")
@Slf4j
@Tag(name = "Articles", description = "Gestión de artículos y alertas del sistema")
public class ArticleController {
     
    private ArticleService articleService;
    private AlertRepository alertRepository;
    private LoanArticleService loanArticleService;
    private ArticleFileGenerationService fileGenerationService;
    private ArticleLoanStatsRepository statsRepository;
    private static final String ERROR_KEY = "Error";
    private static final String MESSAGE_KEY = "Message";
    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    public ArticleController(ArticleService articleService, AlertRepository alertRepository, LoanArticleService loanArticleService,ArticleFileGenerationService fileGenerationService,ArticleLoanStatsRepository statsRepository)  {
        this.articleService = articleService;
        this.alertRepository = alertRepository;
        this.loanArticleService = loanArticleService;
        this.fileGenerationService=fileGenerationService;
        this.statsRepository=statsRepository;
    }

    @GetMapping
    @Operation(
        summary = "Buscar u obtener artículos", 
        description = """
            Permite buscar artículos por diferentes criterios:
            - Sin parámetros: Retorna todos los artículos
            - Número: Búsqueda por ID
            - Estado válido: Filtra por estado (Disponible, Dañado, etc.)
            - 'disponibles:nombre': Filtra por nombre y estado Disponible
            - Otros textos: Búsqueda por nombre
            """,
        parameters = {
            @Parameter(
                name = "q",
                description = "Criterio de búsqueda (ID, estado, nombre, o 'disponibles:nombre')",
                examples = {
                    @ExampleObject(name = "Todos", value = ""),
                    @ExampleObject(name = "Por ID", value = "1"),
                    @ExampleObject(name = "Por estado", value = "Disponible"),
                    @ExampleObject(name = "Disponibles por nombre", value = "disponibles:Balon"),
                    @ExampleObject(name = "Por nombre", value = "Lazo")
                }
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Artículos encontrados",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "cantidad": 2,
                            "articulos": [
                                {
                                    "id": 1,
                                    "name": "Balon",
                                    "articleStatus": "Disponible",
                                    "description": "Balon Golty",
                                    "imageUrl": "/images/Balon.png"
                                },
                                {
                                    "id": 2,
                                    "name": "Raqueta",
                                    "articleStatus": "Disponible",
                                    "description": "Raqueta de ping pong",
                                    "imageUrl": "/images/Raqueta.png"
                                }
                            ]
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Artículo no encontrado",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "error": "Artículo no encontrado",
                            "details": "No se encontró el artículo con ID: 99"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Error interno",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "error": "Error al obtener artículos",
                            "details": "Error de conexión a la base de datos"
                        }
                        """
                    )
                )
            )
        }
    )
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
    @Operation(
        summary = "Crear artículo",
        description = "Registra un nuevo artículo en el sistema",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del artículo a crear",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ArticleDto.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "name": "Balon Golty'",
                        "articleStatus": "Disponible",
                        "description": "Balon profesional",
                        "imageUrl": null
                    }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Artículo creado",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "id": 3,
                            "name": "Balon Golty'",
                            "articleStatus": "Disponible",
                            "description": "Balon profesional",
                            "imageUrl": "/images/monitor_24.png"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Datos inválidos",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "Error": "Error al guardar el articulo",
                            "Message": "El nombre del articulo no puede tener mas de 500 caracteres"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Error interno",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "Error": "Error al guardar el articulo",
                            "Message": "Error inesperado: NullPointerException"
                        }
                        """
                    )
                )
            )
        }
    )
    public ResponseEntity<Object> save(
        @Parameter(description = "Detalles del artículo a crear", required = true) @Valid @RequestBody ArticleDto articleDto) {
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
    @Operation(
        summary = "Actualizar artículo",
        description = "Actualiza los datos de un artículo existente",
        parameters = {
            @Parameter(
                name = "id",
                description = "ID del artículo a actualizar",
                required = true,
                example = "1"
            )
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nuevos datos del artículo",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ArticleDto.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "name": "Balon Actualizada",
                        "articleStatus": "RequireMantenimiento",
                        "description": "Requiere mantenimiento preventivo",
                        "imageUrl": null
                    }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Artículo actualizado",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "id": 1,
                            "name": "Balon Actualizada",
                            "articleStatus": "RequireMantenimiento",
                            "description": "Requiere mantenimiento preventivo",
                            "imageUrl": "/images/Balon_actualizada.png"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Artículo no encontrado",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "Error": "Error al actualizar el articulo",
                            "Message": "Articulo no encontrado con ID: 99"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Error interno",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "Error": "Error al actualizar el articulo",
                            "Message": "Error de conexión a la base de datos"
                        }
                        """
                    )
                )
            )
        }
    )
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
    @Operation(
        summary = "Eliminar artículo",
        description = "Elimina un artículo del sistema usando su ID",
        parameters = {
            @Parameter(
                name = "id",
                description = "ID del artículo a eliminar",
                required = true,
                example = "1"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Artículo eliminado",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "message": "Articulo eliminado correctamente"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Artículo no encontrado",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "Error": "Error al eliminar el articulo",
                            "Message": "Articulo no encontrado con ID: 99"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Error interno",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "Error": "Error al eliminar el articulo",
                            "Message": "Error de conexión a la base de datos"
                        }
                        """
                    )
                )
            )
        }
    )
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
    @Operation(
        summary = "Obtener alertas",
        description = "Recupera todas las alertas generadas por el sistema",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Alertas obtenidas",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        [
                            {
                                "id": "65a1f3e8d4e8b10c9c8b4567",
                                "relatedEntity": "Balon",
                                "message": "Quedan 1 disponibles",
                                "timestamp": "2024-01-12T15:30:45"
                            }
                        ]
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Error interno",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "Error": "Error al obtener las alertas",
                            "Message": "Error de conexión a la base de datos"
                        }
                        """
                    )
                )
            )
        }
    )
    public ResponseEntity<?> getAllAlerts() {
        try {
            return new ResponseEntity<>(alertRepository.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("Error ", "Error al obtener las alertas");
            errorResponse.put("Message ", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Estadísticas de préstamos por artículo",
            description = "Devuelve un listado de artículos con el número de veces que cada uno ha sido prestado. " +
                    "También genera informes en PDF y Excel que pueden ser descargados.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Estadísticas obtenidas exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "totalArticulos": 3,
                        "estadisticas": [
                            {
                                "id": 1,
                                "name": "Balon",
                                "description": "Balon Golty",
                                "articleStatus": "Disponible",
                                "imageUrl": "/images/Balon.png",
                                "vecesPrestado": 12
                            },
                            {
                                "id": 2,
                                "name": "Raqueta",
                                "description": "Raqueta de ping pong",
                                "articleStatus": "Disponible",
                                "imageUrl": "/images/Raqueta.png",
                                "vecesPrestado": 5
                            },
                            {
                                "id": 3,
                                "name": "Lazo",
                                "description": "Lazo para saltar",
                                "articleStatus": "Disponible", 
                                "imageUrl": "/images/lazo.png",
                                "vecesPrestado": 8
                            }
                        ],
                        "reportId": "65a1f3e8d4e8b10c9c8b4567",
                        "downloadLinks": {
                            "pdf": "/Article/stats/pdf/65a1f3e8d4e8b10c9c8b4567",
                            "excel": "/Article/stats/excel/65a1f3e8d4e8b10c9c8b4567"
                        }
                    }
                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "Error": "Error al obtener estadísticas de préstamos",
                        "Message": "Error interno del servidor"
                    }
                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> getArticleLoanStats(
            @RequestParam(name = "username", required = false) String username) {
        try {
            logger.info("Obteniendo estadísticas de préstamos por artículo");

            // Si no se proporciona nombre de usuario, usar un valor por defecto
            String reportGeneratedBy = (username != null && !username.isEmpty())
                    ? username : "Juan-cely-l"; // Usando el nombre de usuario por defecto o el proporcionado

            // Obtener todos los artículos
            List<Article> articles = articleService.getAll();

            // Obtener todos los préstamos
            List<LoanArticle> allLoans = loanArticleService.getLoans(null);

            // Calcular cuántas veces se prestó cada artículo
            Map<Integer, Long> loanCountByArticleId = new HashMap<>();

            for (LoanArticle loan : allLoans) {
                if (loan.getArticleIds() != null) {
                    for (Integer articleId : loan.getArticleIds()) {
                        loanCountByArticleId.put(articleId,
                                loanCountByArticleId.getOrDefault(articleId, 0L) + 1);
                    }
                }
            }

            // Crear respuesta con estadísticas
            List<Map<String, Object>> stats = new ArrayList<>();
            for (Article article : articles) {
                Map<String, Object> articleStat = new HashMap<>();
                articleStat.put("id", article.getId());
                articleStat.put("name", article.getName());
                articleStat.put("description", article.getDescription());
                articleStat.put("articleStatus", article.getArticleStatus());
                articleStat.put("imageUrl", article.getImageUrl());
                articleStat.put("vecesPrestado", loanCountByArticleId.getOrDefault(article.getId(), 0L));

                stats.add(articleStat);
            }

            // Ordenar por número de préstamos (descendente)
            stats.sort((a, b) -> Long.compare(
                    (Long)b.get("vecesPrestado"),
                    (Long)a.get("vecesPrestado")
            ));

            // Crear y guardar reporte de estadísticas
            ArticleLoanStats statsReport = new ArticleLoanStats();
            statsReport.setTitle("Estadísticas de Préstamos de Artículos");
            statsReport.setTotalArticles(articles.size());
            statsReport.setGenerationDate(LocalDateTime.now());
            statsReport.setGeneratedBy(reportGeneratedBy);
            statsReport.setStatistics(stats);

            try {
                // Generar archivos PDF y Excel
                byte[] pdfContent = fileGenerationService.generateArticleStatsPdf(statsReport);
                byte[] excelContent = fileGenerationService.generateArticleStatsExcel(statsReport);

                // Guardar archivos en el reporte
                statsReport.setPdfFile(pdfContent);
                statsReport.setExcelFile(excelContent);

                // Guardar reporte en base de datos
                statsReport = statsRepository.save(statsReport);

            } catch (IOException e) {
                logger.error("Error al generar archivos de reporte: {}", e.getMessage(), e);
                // Continuar con la respuesta JSON incluso si los archivos no se generaron
            }

            // Armar respuesta final
            Map<String, Object> response = new HashMap<>();
            response.put("totalArticulos", articles.size());
            response.put("estadisticas", stats);

            // Si se guardó el reporte, incluir ID y enlaces de descarga
            if (statsReport.getId() != null) {
                response.put("reportId", statsReport.getId());

                Map<String, String> downloadLinks = new HashMap<>();
                downloadLinks.put("pdf", "/Article/stats/pdf/" + statsReport.getId());
                downloadLinks.put("excel", "/Article/stats/excel/" + statsReport.getId());
                response.put("downloadLinks", downloadLinks);
            }

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de préstamos: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put(ERROR_KEY, "Error al obtener estadísticas de préstamos");
            errorResponse.put(MESSAGE_KEY, e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/stats/pdf/{id}")
    @Operation(
            summary = "Descargar reporte de estadísticas en formato PDF",
            description = "Permite descargar un archivo PDF con el reporte de estadísticas de préstamos",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID del reporte de estadísticas",
                            required = true,
                            example = "65a1f3e8d4e8b10c9c8b4567"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "PDF descargado exitosamente",
                            content = @Content(mediaType = "application/pdf")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Reporte no encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "Error": "Reporte no encontrado",
                        "Message": "No existe un reporte con el ID especificado"
                    }
                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<byte[]> getStatsPdf(@PathVariable String id) {
        try {
            ArticleLoanStats stats = statsRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Reporte no encontrado con ID: " + id));

            if (stats.getPdfFile() == null || stats.getPdfFile().length == 0) {
                throw new IllegalStateException("El archivo PDF no está disponible para este reporte");
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=articulos_prestamos_stats_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(stats.getPdfFile());
        } catch (NoSuchElementException e) {
            logger.error("Reporte no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error al obtener archivo PDF: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats/excel/{id}")
    @Operation(
            summary = "Descargar reporte de estadísticas en formato Excel",
            description = "Permite descargar un archivo Excel con el reporte de estadísticas de préstamos",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID del reporte de estadísticas",
                            required = true,
                            example = "65a1f3e8d4e8b10c9c8b4567"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Excel descargado exitosamente",
                            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Reporte no encontrado",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                    {
                        "Error": "Reporte no encontrado",
                        "Message": "No existe un reporte con el ID especificado"
                    }
                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<byte[]> getStatsExcel(@PathVariable String id) {
        try {
            ArticleLoanStats stats = statsRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Reporte no encontrado con ID: " + id));

            if (stats.getExcelFile() == null || stats.getExcelFile().length == 0) {
                throw new IllegalStateException("El archivo Excel no está disponible para este reporte");
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=articulos_prestamos_stats_" + id + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(stats.getExcelFile());
        } catch (NoSuchElementException e) {
            logger.error("Reporte no encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error al obtener archivo Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


}

