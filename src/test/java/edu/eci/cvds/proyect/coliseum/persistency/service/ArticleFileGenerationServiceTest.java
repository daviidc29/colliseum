package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.entity.ArticleLoanStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ArticleFileGenerationServiceTest {

    @InjectMocks
    private ArticleFileGenerationService fileGenerationService;

    private ArticleLoanStats sampleStats;

    @BeforeEach
    void setUp() {
        // Crear un objeto ArticleLoanStats con datos de prueba
        sampleStats = new ArticleLoanStats();
        sampleStats.setId("report123");
        sampleStats.setTitle("Reporte de Prueba");
        sampleStats.setTotalArticles(5);
        sampleStats.setGenerationDate(LocalDateTime.of(2025, 5, 25, 16, 30, 0));
        sampleStats.setGeneratedBy("Usuario de Prueba");

        // Crear estadísticas de ejemplo
        List<Map<String, Object>> statistics = new ArrayList<>();

        Map<String, Object> article1 = new HashMap<>();
        article1.put("id", "art001");
        article1.put("name", "Laptop HP");
        article1.put("articleStatus", "Disponible");
        article1.put("description", "Laptop para préstamo a estudiantes");
        article1.put("vecesPrestado", 15);
        statistics.add(article1);

        Map<String, Object> article2 = new HashMap<>();
        article2.put("id", "art002");
        article2.put("name", "Proyector Epson");
        article2.put("articleStatus", "Prestado");
        article2.put("description", "Proyector para aulas");
        article2.put("vecesPrestado", 30);
        statistics.add(article2);

        Map<String, Object> article3 = new HashMap<>();
        article3.put("id", "art003");
        article3.put("name", "Adaptador HDMI");
        article3.put("articleStatus", "Disponible");
        article3.put("description", null); // Probar con descripción nula
        article3.put("vecesPrestado", 45);
        statistics.add(article3);

        sampleStats.setStatistics(statistics);
    }

    @Test
    @DisplayName("Debería generar correctamente un archivo PDF con estadísticas")
    void shouldGenerateArticleStatsPdf() throws IOException {
        // Ejecutar
        byte[] pdfBytes = fileGenerationService.generateArticleStatsPdf(sampleStats);

        // Verificar
        assertNotNull(pdfBytes, "El array de bytes del PDF no debería ser nulo");
        assertTrue(pdfBytes.length > 0, "El PDF generado debería tener contenido");

        // Verificar que comienza con la firma de un archivo PDF (%PDF-)
        byte[] pdfSignature = {0x25, 0x50, 0x44, 0x46, 0x2D}; // %PDF-
        byte[] firstFiveBytes = new byte[5];
        System.arraycopy(pdfBytes, 0, firstFiveBytes, 0, 5);
        assertArrayEquals(pdfSignature, firstFiveBytes, "El archivo debe comenzar con la firma de PDF");
    }

    @Test
    @DisplayName("Debería manejar estadísticas vacías al generar PDF")
    void shouldHandleEmptyStatisticsForPdf() throws IOException {
        // Preparar un objeto con estadísticas vacías
        sampleStats.setStatistics(new ArrayList<>());

        // Ejecutar
        byte[] pdfBytes = fileGenerationService.generateArticleStatsPdf(sampleStats);

        // Verificar
        assertNotNull(pdfBytes, "El array de bytes del PDF no debería ser nulo incluso con estadísticas vacías");
        assertTrue(pdfBytes.length > 0, "El PDF generado debería tener contenido incluso con estadísticas vacías");
    }

    @Test
    @DisplayName("Debería generar correctamente un archivo Excel con estadísticas")
    void shouldGenerateArticleStatsExcel() throws IOException {
        // Ejecutar
        byte[] excelBytes = fileGenerationService.generateArticleStatsExcel(sampleStats);

        // Verificar
        assertNotNull(excelBytes, "El array de bytes del Excel no debería ser nulo");
        assertTrue(excelBytes.length > 0, "El Excel generado debería tener contenido");

        // Verificar que comienza con la firma de un archivo XLSX (PK..)
        byte[] xlsxSignature = {0x50, 0x4B, 0x03, 0x04}; // PK..
        byte[] firstFourBytes = new byte[4];
        System.arraycopy(excelBytes, 0, firstFourBytes, 0, 4);
        assertArrayEquals(xlsxSignature, firstFourBytes, "El archivo debe comenzar con la firma de XLSX (PK..)");
    }

    @Test
    @DisplayName("Debería manejar estadísticas vacías al generar Excel")
    void shouldHandleEmptyStatisticsForExcel() throws IOException {
        // Preparar un objeto con estadísticas vacías
        sampleStats.setStatistics(new ArrayList<>());

        // Ejecutar
        byte[] excelBytes = fileGenerationService.generateArticleStatsExcel(sampleStats);

        // Verificar
        assertNotNull(excelBytes, "El array de bytes del Excel no debería ser nulo incluso con estadísticas vacías");
        assertTrue(excelBytes.length > 0, "El Excel generado debería tener contenido incluso con estadísticas vacías");
    }

    @Test
    @DisplayName("Debería manejar estadísticas nulas al generar PDF")
    void shouldHandleNullStatisticsForPdf() {
        // Preparar un objeto con estadísticas nulas
        sampleStats.setStatistics(null);

        // Verificar que lanza NullPointerException
        Exception exception = assertThrows(NullPointerException.class, () -> {
            fileGenerationService.generateArticleStatsPdf(sampleStats);
        });

        assertNotNull(exception, "Debería lanzar NullPointerException cuando statistics es null");
    }

    @Test
    @DisplayName("Debería manejar estadísticas nulas al generar Excel")
    void shouldHandleNullStatisticsForExcel() {
        // Preparar un objeto con estadísticas nulas
        sampleStats.setStatistics(null);

        // Verificar que lanza NullPointerException
        Exception exception = assertThrows(NullPointerException.class, () -> {
            fileGenerationService.generateArticleStatsExcel(sampleStats);
        });

        assertNotNull(exception, "Debería lanzar NullPointerException cuando statistics es null");
    }

    @Test
    @DisplayName("Debería manejar valores nulos o vacíos en los elementos de estadísticas para PDF")
    void shouldHandleNullValuesInStatisticsElementsForPdf() throws IOException {
        // Preparar un mapa con valores nulos
        List<Map<String, Object>> statistics = new ArrayList<>();
        Map<String, Object> articleWithNulls = new HashMap<>();
        articleWithNulls.put("id", null);
        articleWithNulls.put("name", "");
        articleWithNulls.put("articleStatus", null);
        // No incluimos description para probar el getOrDefault
        articleWithNulls.put("vecesPrestado", null);
        statistics.add(articleWithNulls);
        sampleStats.setStatistics(statistics);

        // Ejecutar - no debería lanzar excepción
        byte[] pdfBytes = fileGenerationService.generateArticleStatsPdf(sampleStats);

        // Verificar
        assertNotNull(pdfBytes, "El array de bytes del PDF no debería ser nulo");
        assertTrue(pdfBytes.length > 0, "El PDF generado debería tener contenido");
    }

    @Test
    @DisplayName("Debería manejar valores nulos o vacíos en los elementos de estadísticas para Excel")
    void shouldHandleNullValuesInStatisticsElementsForExcel() throws IOException {
        // Preparar un mapa con valores nulos
        List<Map<String, Object>> statistics = new ArrayList<>();
        Map<String, Object> articleWithNulls = new HashMap<>();
        articleWithNulls.put("id", null);
        articleWithNulls.put("name", "");
        articleWithNulls.put("articleStatus", null);
        // No incluimos description para probar el getOrDefault
        articleWithNulls.put("vecesPrestado", null);
        statistics.add(articleWithNulls);
        sampleStats.setStatistics(statistics);

        // Ejecutar - no debería lanzar excepción
        byte[] excelBytes = fileGenerationService.generateArticleStatsExcel(sampleStats);

        // Verificar
        assertNotNull(excelBytes, "El array de bytes del Excel no debería ser nulo");
        assertTrue(excelBytes.length > 0, "El Excel generado debería tener contenido");
    }
}