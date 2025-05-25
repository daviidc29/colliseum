package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.LoanArticle;
import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanArcticleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanArticleServiceTest {

    @Mock
    private LoanArcticleRepository loanArcticleRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private LoanArticleService loanArticleService;

    @Captor
    private ArgumentCaptor<LoanArticle> loanArticleCaptor;

    @Captor
    private ArgumentCaptor<List<Article>> articleListCaptor;

    @Captor
    private ArgumentCaptor<Alert> alertCaptor;

    private LoanArticle sampleLoan;
    private Article sampleArticle1;
    private Article sampleArticle2;
    private List<Integer> articleIds;

    @BeforeEach
    void setUp() {
        // Configurar objetos de muestra para las pruebas
        articleIds = Arrays.asList(1, 2);

        sampleArticle1 = new Article();
        sampleArticle1.setId(1);
        sampleArticle1.setName("Laptop");
        sampleArticle1.setArticleStatus("Disponible");

        sampleArticle2 = new Article();
        sampleArticle2.setId(2);
        sampleArticle2.setName("Proyector");
        sampleArticle2.setArticleStatus("Disponible");

        sampleLoan = LoanArticle.builder()
                .id("loan123")
                .nameUser("Juan Cely")
                .userId("user123")
                .userRole("Estudiante")
                .articleIds(articleIds)
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .loanDescriptionType("Préstamo para proyecto")
                .loanDate(LocalDate.now())
                .devolutionDate(LocalDate.now().plusDays(7))
                .build();
    }

    @Test
    @DisplayName("Crear préstamo - caso exitoso")
    void testCreateLoanSuccess() {
        // Preparar
        when(loanArcticleRepository.findActiveLoans(anyString())).thenReturn(Collections.emptyList());
        when(articleRepository.findAllById(anyList())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));
        when(loanArcticleRepository.save(any(LoanArticle.class))).thenReturn(sampleLoan);

        // Ejecutar
        LoanArticle result = loanArticleService.createLoan(sampleLoan);

        // Verificar
        assertNotNull(result);
        assertEquals(sampleLoan.getId(), result.getId());

        verify(loanArcticleRepository).findActiveLoans(sampleLoan.getUserId());
        // Corregido: verificar que findAllById es llamado 2 veces
        verify(articleRepository, times(2)).findAllById(sampleLoan.getArticleIds());
        verify(loanArcticleRepository).save(any(LoanArticle.class));

        // Verificar que se actualizó el estado de los artículos
        verify(articleRepository).saveAll(articleListCaptor.capture());
        List<Article> updatedArticles = articleListCaptor.getValue();
        assertEquals(2, updatedArticles.size());
        assertEquals("Prestado", updatedArticles.get(0).getArticleStatus());
        assertEquals("Prestado", updatedArticles.get(1).getArticleStatus());
    }

    @Test
    @DisplayName("Crear préstamo - usuario con préstamo activo")
    void testCreateLoanUserHasActiveLoan() {
        // Preparar
        LoanArticle activeLoan = LoanArticle.builder()
                .id("activeId")
                .nameUser("Juan Cely")
                .userId("user123")
                .userRole("Estudiante")
                .articleIds(Arrays.asList(3, 4))
                .loanStatus("Prestado")
                .loanDate(LocalDate.now().minusDays(3))
                .build();

        when(loanArcticleRepository.findActiveLoans(anyString())).thenReturn(Collections.singletonList(activeLoan));

        // Ejecutar y verificar
        LoanException.LoanExceptionStateError exception = assertThrows(
                LoanException.LoanExceptionStateError.class,
                () -> loanArticleService.createLoan(sampleLoan)
        );

        // Verificar que el mensaje contiene información sobre el préstamo activo
        assertTrue(exception.getMessage().contains("ya tiene un préstamo activo"));
        assertTrue(exception.getMessage().contains(activeLoan.getId()));
    }

    @Test
    @DisplayName("Crear préstamo - artículos no disponibles")
    void testCreateLoanArticlesNotAvailable() {
        // Preparar
        when(loanArcticleRepository.findActiveLoans(anyString())).thenReturn(Collections.emptyList());

        sampleArticle1.setArticleStatus("Prestado");
        when(articleRepository.findAllById(anyList())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));

        // Ejecutar y verificar
        LoanException.LoanExceptionBookIsAvailable exception = assertThrows(
                LoanException.LoanExceptionBookIsAvailable.class,
                () -> loanArticleService.createLoan(sampleLoan)
        );

        assertTrue(exception.getMessage().contains("no están disponibles"));
    }

    @Test
    @DisplayName("Crear préstamo - validación de fechas")
    void testCreateLoanInvalidDates() {
        // Preparar
        when(loanArcticleRepository.findActiveLoans(anyString())).thenReturn(Collections.emptyList());
        when(articleRepository.findAllById(anyList())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));

        // Configurar fechas inválidas (devolución antes de préstamo)
        sampleLoan.setLoanDate(LocalDate.now().plusDays(5));
        sampleLoan.setDevolutionDate(LocalDate.now());

        // Ejecutar y verificar
        LoanException.LoanExceptionTimeError exception = assertThrows(
                LoanException.LoanExceptionTimeError.class,
                () -> loanArticleService.createLoan(sampleLoan)
        );

        assertTrue(exception.getMessage().contains("La fecha de préstamo no puede ser posterior a la de devolución"));
    }

    @Test
    @DisplayName("Devolver préstamo - caso exitoso")
    void testDevolverLoanSuccess() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));
        when(articleRepository.findAllById(anyList())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));

        // Ejecutar
        loanArticleService.devolverLoan("loan123");

        // Verificar
        verify(loanArcticleRepository).save(loanArticleCaptor.capture());
        LoanArticle updatedLoan = loanArticleCaptor.getValue();

        assertEquals("Devuelto", updatedLoan.getLoanStatus());
        assertEquals(LocalDate.now(), updatedLoan.getDevolutionDate());
        assertNotNull(updatedLoan.getDevolutionRsegister());
        assertTrue(updatedLoan.getDevolutionRsegister().contains("Devolución realizada el"));

        // Verificar que se actualizó el estado de los artículos
        verify(articleRepository).saveAll(articleListCaptor.capture());
        List<Article> updatedArticles = articleListCaptor.getValue();
        assertEquals(2, updatedArticles.size());
        assertEquals("Disponible", updatedArticles.get(0).getArticleStatus());
        assertEquals("Disponible", updatedArticles.get(1).getArticleStatus());
    }

    @Test
    @DisplayName("Determinar estado del artículo basado en el estado del equipo")
    void testDetermineArticleStatus() {
        assertEquals("Disponible", loanArticleService.determineArticleStatus("En buen estado"));
        assertEquals("Dañado", loanArticleService.determineArticleStatus("Dañado"));
        assertEquals("RequiereMantenimiento", loanArticleService.determineArticleStatus("Requiere mantenimiento"));
        assertEquals("Disponible", loanArticleService.determineArticleStatus(null));
    }

    @Test
    @DisplayName("Eliminar préstamo - caso exitoso")
    void testDeleteLoanById() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));
        // Configurar el mock para articleRepository.findAllById() que es llamado dentro de updateArticlesStatus()
        when(articleRepository.findAllById(sampleLoan.getArticleIds())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));

        // Asegurar que el préstamo tiene estado "Prestado" para que se actualicen los artículos
        sampleLoan.setLoanStatus("Prestado");

        // Ejecutar
        LoanArticle result = loanArticleService.deleteLoanById("loan123");

        // Verificar
        assertNotNull(result);
        assertEquals(sampleLoan.getId(), result.getId());

        // Verificar que se actualizó el estado de los artículos y se eliminó el préstamo
        verify(articleRepository).saveAll(articleListCaptor.capture());
        List<Article> updatedArticles = articleListCaptor.getValue();
        assertEquals(2, updatedArticles.size());
        assertEquals("Disponible", updatedArticles.get(0).getArticleStatus());
        assertEquals("Disponible", updatedArticles.get(1).getArticleStatus());

        verify(loanArcticleRepository).delete(sampleLoan);
    }

    @Test
    @DisplayName("Eliminar préstamo devuelto - debe fallar")
    void testDeleteReturnedLoan() {
        // Preparar
        sampleLoan.setLoanStatus("Devuelto");
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));

        // Ejecutar y verificar
        LoanException.LoanExceptionStateError exception = assertThrows(
                LoanException.LoanExceptionStateError.class,
                () -> loanArticleService.deleteLoanById("loan123")
        );

        assertTrue(exception.getMessage().contains("No se puede eliminar un préstamo devuelto"));
    }

    @Test
    @DisplayName("Actualizar préstamo - cambio de estado")
    void testUpdateLoanChangeStatus() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));
        when(articleRepository.findAllById(anyList())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Devuelto");

        // Ejecutar
        loanArticleService.updateLoan("loan123", updates);

        // Verificar
        // Corregido: verificar que save() es llamado exactamente 2 veces
        verify(loanArcticleRepository, times(2)).save(any(LoanArticle.class));

        // Verificar que se actualizó el estado de los artículos
        verify(articleRepository, atLeastOnce()).saveAll(any());
    }

    @Test
    @DisplayName("Actualizar préstamo - campo observaciones")
    void testUpdateLoanObservaciones() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));

        Map<String, Object> updates = new HashMap<>();
        updates.put("observaciones", "Nueva descripción del préstamo");

        // Ejecutar
        loanArticleService.updateLoan("loan123", updates);

        // Verificar
        verify(loanArcticleRepository).save(loanArticleCaptor.capture());
        LoanArticle updatedLoan = loanArticleCaptor.getValue();

        assertEquals("Nueva descripción del préstamo", updatedLoan.getLoanDescriptionType());
    }

    @Test
    @DisplayName("Actualizar préstamo - fecha de devolución")
    void testUpdateLoanDevolutionDate() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));

        LocalDate newDate = LocalDate.now().plusDays(14);
        Map<String, Object> updates = new HashMap<>();
        updates.put("fecha_devolucion", newDate.toString());

        // Ejecutar
        loanArticleService.updateLoan("loan123", updates);

        // Verificar
        verify(loanArcticleRepository).save(loanArticleCaptor.capture());
        LoanArticle updatedLoan = loanArticleCaptor.getValue();

        assertEquals(newDate, updatedLoan.getDevolutionDate());
    }

    @Test
    @DisplayName("Actualizar préstamo - estado del equipo")
    void testUpdateLoanEquipmentStatus() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));
        when(articleRepository.findAllById(anyList())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));

        Map<String, Object> updates = new HashMap<>();
        updates.put("equipmentStatus", "Requiere mantenimiento");

        // Ejecutar
        loanArticleService.updateLoan("loan123", updates);

        // Verificar
        verify(loanArcticleRepository).save(loanArticleCaptor.capture());
        LoanArticle updatedLoan = loanArticleCaptor.getValue();

        assertEquals("Requiere mantenimiento", updatedLoan.getEquipmentStatus());

        // Verificar que se actualizó el estado de los artículos
        verify(articleRepository).saveAll(articleListCaptor.capture());
        List<Article> updatedArticles = articleListCaptor.getValue();
        assertEquals("RequiereMantenimiento", updatedArticles.get(0).getArticleStatus());
    }

    @Test
    @DisplayName("Actualizar estado de artículos individualmente")
    void testUpdateArticleStatesFromMap() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));
        when(articleRepository.findById(1)).thenReturn(Optional.of(sampleArticle1));
        when(articleRepository.findById(2)).thenReturn(Optional.of(sampleArticle2));

        Map<String, Object> updates = new HashMap<>();
        Map<String, String> articuloEstados = new HashMap<>();
        articuloEstados.put("1", "Dañado");
        articuloEstados.put("2", "Disponible");
        updates.put("articulo_estado", articuloEstados);

        // Ejecutar
        loanArticleService.updateLoan("loan123", updates);

        // Verificar
        verify(articleRepository, times(2)).save(any(Article.class));
    }

    @Test
    @DisplayName("Actualizar estado de artículos - artículo inválido")
    void testUpdateArticleStatesInvalidArticle() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));

        Map<String, Object> updates = new HashMap<>();
        Map<String, String> articuloEstados = new HashMap<>();
        articuloEstados.put("99", "Dañado"); // ID no pertenece al préstamo
        updates.put("articulo_estado", articuloEstados);

        // Ejecutar y verificar
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanArticleService.updateLoan("loan123", updates)
        );

        assertTrue(exception.getMessage().contains("El artículo 99 no pertenece al préstamo"));
    }

    @Test
    @DisplayName("Marcar préstamo como vencido")
    void testMarkAsVencido() {
        // Preparar
        // Eliminado el stub innecesario de findById
        when(articleRepository.findAllById(anyList())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));

        // Ejecutar
        loanArticleService.markAsVencido(sampleLoan);

        // Verificar
        verify(loanArcticleRepository).save(loanArticleCaptor.capture());
        LoanArticle updatedLoan = loanArticleCaptor.getValue();

        assertEquals("Vencido", updatedLoan.getLoanStatus());

        // Verificar que se actualizó el estado de los artículos
        verify(articleRepository).saveAll(articleListCaptor.capture());
        List<Article> updatedArticles = articleListCaptor.getValue();
        assertEquals("Disponible", updatedArticles.get(0).getArticleStatus());

        // Verificar que se guardó una alerta
        verify(alertRepository).save(alertCaptor.capture());
        Alert alert = alertCaptor.getValue();
        assertNotNull(alert);

        // Verificar que el mensaje de la alerta contiene el ID del préstamo
        String expectedAlertMessage = String.format("Préstamo marcado como vencido: %s", sampleLoan.getId());
        assertEquals(expectedAlertMessage, alert.getMessage());
    }
    @Test
    @DisplayName("Verificar préstamos vencidos")
    void testVerificarPrestamosVencidos() {
        // Preparar
        List<LoanArticle> prestamosVencidos = new ArrayList<>();
        prestamosVencidos.add(sampleLoan);

        when(loanArcticleRepository.findByLoanStatusAndDevolutionDateBefore(
                eq("Prestado"), any(LocalDate.class))).thenReturn(prestamosVencidos);
        when(articleRepository.findAllById(anyList())).thenReturn(Arrays.asList(sampleArticle1, sampleArticle2));

        // Ejecutar
        loanArticleService.verificarPrestamosVencidos();

        // Verificar
        verify(loanArcticleRepository).save(any(LoanArticle.class));
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    @DisplayName("Enviar recordatorios de devolución")
    void testEnviarRecordatoriosDevolucion() {
        // Preparar
        List<LoanArticle> prestamos = new ArrayList<>();
        prestamos.add(sampleLoan);

        // Fecha de recordatorio (mañana)
        LocalDate fechaRecordatorio = LocalDate.now().plusDays(1);

        when(loanArcticleRepository.findByLoanStatusAndDevolutionDate(
                eq("Prestado"), eq(fechaRecordatorio))).thenReturn(prestamos);

        // Ejecutar
        loanArticleService.enviarRecordatoriosDevolucion();

        // Verificar
        verify(alertRepository).save(alertCaptor.capture());
        Alert alert = alertCaptor.getValue();
        assertNotNull(alert);

        // Verificar el mensaje de la alerta
        String expectedMessage = "Recordatorio: Devolución pendiente para mañana (" + fechaRecordatorio + ")";
        assertEquals(expectedMessage, alert.getMessage());

        // Verificar que el campo description contiene el id del usuario (según la implementación)
        assertEquals(sampleLoan.getUserId(), alert.getDescription());
    }

    @Test
    @DisplayName("Obtener préstamos por estado")
    void testGetLoans() {
        // Preparar
        List<LoanArticle> prestados = Collections.singletonList(sampleLoan);
        when(loanArcticleRepository.findByLoanStatus("Prestado")).thenReturn(prestados);

        // Ejecutar
        List<LoanArticle> result = loanArticleService.getLoans("Prestado");

        // Verificar
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleLoan.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Obtener préstamos por usuario")
    void testGetLoansByUser() {
        // Preparar
        List<LoanArticle> loansForUser = Collections.singletonList(sampleLoan);
        when(loanArcticleRepository.findByUserId("user123")).thenReturn(loansForUser);

        // Ejecutar
        List<LoanArticle> result = loanArticleService.getLoansByUser("user123");

        // Verificar
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleLoan.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Obtener préstamos por usuario - sin préstamos")
    void testGetLoansByUserNoLoans() {
        // Preparar
        when(loanArcticleRepository.findByUserId("user456")).thenReturn(Collections.emptyList());

        // Ejecutar y verificar
        LoanException.LoanExceptionEstudianteHasNotPrestamo exception = assertThrows(
                LoanException.LoanExceptionEstudianteHasNotPrestamo.class,
                () -> loanArticleService.getLoansByUser("user456")
        );

        assertTrue(exception.getMessage().contains("no tiene préstamos registrados"));
    }

    @Test
    @DisplayName("Obtener préstamo por ID")
    void testGetLoanById() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));

        // Ejecutar
        LoanArticle result = loanArticleService.getLoanById("loan123");

        // Verificar
        assertNotNull(result);
        assertEquals(sampleLoan.getId(), result.getId());
    }

    @Test
    @DisplayName("Obtener préstamo por ID - no encontrado")
    void testGetLoanByIdNotFound() {
        // Preparar
        when(loanArcticleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Ejecutar y verificar
        LoanException.LoanExceptionPrestamoIdNotFound exception = assertThrows(
                LoanException.LoanExceptionPrestamoIdNotFound.class,
                () -> loanArticleService.getLoanById("nonexistent")
        );

        assertTrue(exception.getMessage().contains("Préstamo no encontrado"));
    }

    @Test
    @DisplayName("Obtener artículos disponibles en intervalo")
    void testGetAvailableArticlesInInterval() {
        // Preparar
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        // Fingir que no hay préstamos solapados
        when(loanArcticleRepository.findOverlappingLoans(
                eq("Prestado"), eq(startDate), eq(endDate))).thenReturn(Collections.emptyList());

        List<Article> availableArticles = Arrays.asList(sampleArticle1, sampleArticle2);

        // Mantener el Optional.of() ya que el método lo requiere
        when(articleRepository.findByArticleStatus("Disponible")).thenReturn(Optional.of(availableArticles));

        // Ejecutar
        Object result = loanArticleService.getAvailableArticlesInInterval(startDate, endDate);

        // Verificar
        assertNotNull(result);
        assertTrue(result instanceof Optional<?>);

        Optional<?> optionalResult = (Optional<?>) result;
        assertTrue(optionalResult.isPresent());

        Object listObject = optionalResult.get();
        assertTrue(listObject instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<Article> articleList = (List<Article>) listObject;
        assertEquals(2, articleList.size());
        assertTrue(articleList.contains(sampleArticle1));
        assertTrue(articleList.contains(sampleArticle2));
    }

    @Test
    @DisplayName("Obtener préstamos por rango de fechas y estado")
    void testGetLoansByDateRangeAndStatus() {
        // Preparar
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<LoanArticle> matchingLoans = Collections.singletonList(sampleLoan);
        when(mongoTemplate.find(any(Query.class), eq(LoanArticle.class))).thenReturn(matchingLoans);

        // Ejecutar
        List<LoanArticle> result = loanArticleService.getLoansByDateRangeAndStatus(startDate, endDate, "Prestado");

        // Verificar
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleLoan.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Actualizar estado de artículos en un préstamo")
    void testUpdateArticlesStatus() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));
        when(articleRepository.findById(1)).thenReturn(Optional.of(sampleArticle1));
        when(articleRepository.findById(2)).thenReturn(Optional.of(sampleArticle2));

        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("1", "Dañado");
        articulosUpdate.put("2", "RequiereMantenimiento");

        // Ejecutar
        loanArticleService.updateArticlesStatus("loan123", articulosUpdate);

        // Verificar
        verify(articleRepository, times(2)).save(any(Article.class));
    }

    @Test
    @DisplayName("Actualizar estado de artículos - artículo no pertenece al préstamo")
    void testUpdateArticlesStatusInvalidArticle() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));

        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("99", "Dañado"); // ID no pertenece al préstamo

        // Ejecutar y verificar
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanArticleService.updateArticlesStatus("loan123", articulosUpdate)
        );

        assertTrue(exception.getMessage().contains("no pertenece al préstamo"));
    }

    @Test
    @DisplayName("Actualizar estado de artículos - estado inválido")
    void testUpdateArticlesStatusInvalidStatus() {
        // Preparar
        when(loanArcticleRepository.findById("loan123")).thenReturn(Optional.of(sampleLoan));

        Map<String, String> articulosUpdate = new HashMap<>();
        articulosUpdate.put("1", "EstadoInválido");

        // Ejecutar y verificar
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanArticleService.updateArticlesStatus("loan123", articulosUpdate)
        );

        assertTrue(exception.getMessage().contains("Estado inválido para artículo"));
    }

    @Test
    @DisplayName("Validar rango de fechas")
    void testValidateDateRange() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        // Probar fechas válidas (no debería lanzar excepción)
        Object result = loanArticleService.getAvailableArticlesInInterval(startDate, endDate);
        assertNotNull(result);

        // Probar fecha de inicio posterior a la de fin
        LocalDate invalidStartDate = LocalDate.now().plusDays(10);
        LocalDate invalidEndDate = LocalDate.now();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanArticleService.getAvailableArticlesInInterval(invalidStartDate, invalidEndDate)
        );

        assertTrue(exception.getMessage().contains("La fecha de inicio no puede ser posterior a la fecha de fin"));
    }

    @Test
    @DisplayName("Parsear fecha - caso exitoso")
    void testParseDateSuccess() {
        // Probar con string
        LocalDate date1 = LocalDate.now();
        LocalDate result1 = loanArticleService.parseDate(date1.toString());
        assertEquals(date1, result1);

        // Probar con LocalDate
        LocalDate date2 = LocalDate.now().plusDays(5);
        LocalDate result2 = loanArticleService.parseDate(date2);
        assertEquals(date2, result2);
    }

    @Test
    @DisplayName("Parsear fecha - formato inválido")
    void testParseDateInvalidFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanArticleService.parseDate("fecha-invalida")
        );

        assertTrue(exception.getMessage().contains("Formato de fecha inválido"));
    }

    @Test
    @DisplayName("Parsear fecha - tipo inválido")
    void testParseDateInvalidType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanArticleService.parseDate(123)
        );

        assertTrue(exception.getMessage().contains("Tipo de fecha inválido"));
    }
}