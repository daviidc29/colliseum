package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArticleLoanStatsTest {

    private ArticleLoanStats articleLoanStats;
    private String id;
    private String title;
    private int totalArticles;
    private LocalDateTime generationDate;
    private String generatedBy;
    private List<Map<String, Object>> statistics;
    private byte[] pdfFile;
    private byte[] excelFile;

    @BeforeEach
    void setUp() {
        // Inicializar el objeto a probar
        articleLoanStats = new ArticleLoanStats();

        // Preparar datos de prueba
        id = "stats123";
        title = "Informe de préstamos Q2 2025";
        totalArticles = 150;
        generationDate = LocalDateTime.now();
        generatedBy = "admin";

        // Crear lista de estadísticas
        statistics = new ArrayList<>();
        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("category", "Libros");
        stat1.put("count", 75);
        stat1.put("percentage", 50.0);

        Map<String, Object> stat2 = new HashMap<>();
        stat2.put("category", "Equipos electrónicos");
        stat2.put("count", 45);
        stat2.put("percentage", 30.0);

        Map<String, Object> stat3 = new HashMap<>();
        stat3.put("category", "Otros");
        stat3.put("count", 30);
        stat3.put("percentage", 20.0);

        statistics.add(stat1);
        statistics.add(stat2);
        statistics.add(stat3);

        // Datos binarios para archivos
        pdfFile = "contenido PDF simulado".getBytes();
        excelFile = "contenido Excel simulado".getBytes();
    }

    @Test
    @DisplayName("Prueba del constructor por defecto")
    void testConstructor() {
        ArticleLoanStats stats = new ArticleLoanStats();
        assertNotNull(stats, "El objeto ArticleLoanStats debería crearse correctamente");
        assertNull(stats.getId(), "El ID debería ser nulo inicialmente");
        assertNull(stats.getTitle(), "El título debería ser nulo inicialmente");
        assertEquals(0, stats.getTotalArticles(), "El total de artículos debería ser 0 inicialmente");
        assertNull(stats.getGenerationDate(), "La fecha de generación debería ser nula inicialmente");
        assertNull(stats.getGeneratedBy(), "El campo generado por debería ser nulo inicialmente");
        assertNull(stats.getStatistics(), "Las estadísticas deberían ser nulas inicialmente");
        assertNull(stats.getPdfFile(), "El archivo PDF debería ser nulo inicialmente");
        assertNull(stats.getExcelFile(), "El archivo Excel debería ser nulo inicialmente");
    }

    @Test
    @DisplayName("Prueba del setter y getter para id")
    void testSetAndGetId() {
        articleLoanStats.setId(id);
        assertEquals(id, articleLoanStats.getId(), "El valor de ID debería ser el que se estableció");

        // Probar con null
        articleLoanStats.setId(null);
        assertNull(articleLoanStats.getId(), "El valor de ID debería ser null después de establecerlo como null");

        // Probar con cadena vacía
        String emptyId = "";
        articleLoanStats.setId(emptyId);
        assertEquals(emptyId, articleLoanStats.getId(), "El valor de ID debería ser una cadena vacía");
    }

    @Test
    @DisplayName("Prueba del setter y getter para título")
    void testSetAndGetTitle() {
        articleLoanStats.setTitle(title);
        assertEquals(title, articleLoanStats.getTitle(), "El valor del título debería ser el que se estableció");

        // Probar con null
        articleLoanStats.setTitle(null);
        assertNull(articleLoanStats.getTitle(), "El valor del título debería ser null después de establecerlo como null");

        // Probar con título largo
        String longTitle = "Este es un título extremadamente largo que podría exceder cualquier límite razonable para un campo de este tipo en un sistema real";
        articleLoanStats.setTitle(longTitle);
        assertEquals(longTitle, articleLoanStats.getTitle(), "El valor del título largo debería ser el mismo que se estableció");
    }

    @Test
    @DisplayName("Prueba del setter y getter para totalArticles")
    void testSetAndGetTotalArticles() {
        articleLoanStats.setTotalArticles(totalArticles);
        assertEquals(totalArticles, articleLoanStats.getTotalArticles(), "El total de artículos debería ser el que se estableció");

        // Probar con valor cero
        articleLoanStats.setTotalArticles(0);
        assertEquals(0, articleLoanStats.getTotalArticles(), "El total de artículos debería ser 0");

        // Probar con valor negativo (aunque en un caso real podría ser validado)
        int negativeValue = -10;
        articleLoanStats.setTotalArticles(negativeValue);
        assertEquals(negativeValue, articleLoanStats.getTotalArticles(), "El total de artículos debería ser el valor negativo establecido");
    }

    @Test
    @DisplayName("Prueba del setter y getter para generationDate")
    void testSetAndGetGenerationDate() {
        articleLoanStats.setGenerationDate(generationDate);
        assertEquals(generationDate, articleLoanStats.getGenerationDate(), "La fecha de generación debería ser la que se estableció");

        // Probar con null
        articleLoanStats.setGenerationDate(null);
        assertNull(articleLoanStats.getGenerationDate(), "La fecha de generación debería ser null después de establecerlo como null");

        // Probar con una fecha futura
        LocalDateTime futureDate = LocalDateTime.now().plusYears(1);
        articleLoanStats.setGenerationDate(futureDate);
        assertEquals(futureDate, articleLoanStats.getGenerationDate(), "La fecha de generación futura debería ser la que se estableció");
    }

    @Test
    @DisplayName("Prueba del setter y getter para generatedBy")
    void testSetAndGetGeneratedBy() {
        articleLoanStats.setGeneratedBy(generatedBy);
        assertEquals(generatedBy, articleLoanStats.getGeneratedBy(), "El valor de generado por debería ser el que se estableció");

        // Probar con null
        articleLoanStats.setGeneratedBy(null);
        assertNull(articleLoanStats.getGeneratedBy(), "El valor de generado por debería ser null después de establecerlo como null");

        // Probar con cadena vacía
        String emptyGeneratedBy = "";
        articleLoanStats.setGeneratedBy(emptyGeneratedBy);
        assertEquals(emptyGeneratedBy, articleLoanStats.getGeneratedBy(), "El valor de generado por debería ser una cadena vacía");
    }

    @Test
    @DisplayName("Prueba del setter y getter para statistics")
    void testSetAndGetStatistics() {
        articleLoanStats.setStatistics(statistics);
        assertEquals(statistics, articleLoanStats.getStatistics(), "Las estadísticas deberían ser las que se establecieron");
        assertEquals(3, articleLoanStats.getStatistics().size(), "Deberían haber 3 elementos en las estadísticas");

        // Probar con null
        articleLoanStats.setStatistics(null);
        assertNull(articleLoanStats.getStatistics(), "Las estadísticas deberían ser null después de establecerlas como null");

        // Probar con lista vacía
        List<Map<String, Object>> emptyStats = new ArrayList<>();
        articleLoanStats.setStatistics(emptyStats);
        assertEquals(emptyStats, articleLoanStats.getStatistics(), "Las estadísticas deberían ser una lista vacía");
        assertEquals(0, articleLoanStats.getStatistics().size(), "Las estadísticas deberían estar vacías");
    }

    @Test
    @DisplayName("Prueba del setter y getter para pdfFile")
    void testSetAndGetPdfFile() {
        articleLoanStats.setPdfFile(pdfFile);
        assertArrayEquals(pdfFile, articleLoanStats.getPdfFile(), "El archivo PDF debería ser el que se estableció");

        // Probar con null
        articleLoanStats.setPdfFile(null);
        assertNull(articleLoanStats.getPdfFile(), "El archivo PDF debería ser null después de establecerlo como null");

        // Probar con array vacío
        byte[] emptyPdfFile = new byte[0];
        articleLoanStats.setPdfFile(emptyPdfFile);
        assertArrayEquals(emptyPdfFile, articleLoanStats.getPdfFile(), "El archivo PDF debería ser un array vacío");
        assertEquals(0, articleLoanStats.getPdfFile().length, "El archivo PDF debería tener longitud 0");
    }

    @Test
    @DisplayName("Prueba del setter y getter para excelFile")
    void testSetAndGetExcelFile() {
        articleLoanStats.setExcelFile(excelFile);
        assertArrayEquals(excelFile, articleLoanStats.getExcelFile(), "El archivo Excel debería ser el que se estableció");

        // Probar con null
        articleLoanStats.setExcelFile(null);
        assertNull(articleLoanStats.getExcelFile(), "El archivo Excel debería ser null después de establecerlo como null");

        // Probar con array vacío
        byte[] emptyExcelFile = new byte[0];
        articleLoanStats.setExcelFile(emptyExcelFile);
        assertArrayEquals(emptyExcelFile, articleLoanStats.getExcelFile(), "El archivo Excel debería ser un array vacío");
        assertEquals(0, articleLoanStats.getExcelFile().length, "El archivo Excel debería tener longitud 0");
    }

    @Test
    @DisplayName("Prueba de todos los setters en conjunto")
    void testAllSetters() {
        // Configurar todos los campos
        articleLoanStats.setId(id);
        articleLoanStats.setTitle(title);
        articleLoanStats.setTotalArticles(totalArticles);
        articleLoanStats.setGenerationDate(generationDate);
        articleLoanStats.setGeneratedBy(generatedBy);
        articleLoanStats.setStatistics(statistics);
        articleLoanStats.setPdfFile(pdfFile);
        articleLoanStats.setExcelFile(excelFile);

        // Verificar todos los campos
        assertEquals(id, articleLoanStats.getId());
        assertEquals(title, articleLoanStats.getTitle());
        assertEquals(totalArticles, articleLoanStats.getTotalArticles());
        assertEquals(generationDate, articleLoanStats.getGenerationDate());
        assertEquals(generatedBy, articleLoanStats.getGeneratedBy());
        assertEquals(statistics, articleLoanStats.getStatistics());
        assertArrayEquals(pdfFile, articleLoanStats.getPdfFile());
        assertArrayEquals(excelFile, articleLoanStats.getExcelFile());
    }
}