package edu.eci.cvds.proyect.coliseum.persistency.controller;

import edu.eci.cvds.proyect.coliseum.persistency.Controller.LoanController;
import edu.eci.cvds.proyect.coliseum.persistency.Exception.ArticleException;
import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoanControllerTest {

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController loanController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- createLoan ---

    @Test
    void createLoan_Success() {
        Loan loan = new Loan();
        loan.setUserId("U-12345");
        Loan savedLoan = new Loan();
        savedLoan.setId("LN-123");
        when(loanService.createLoan(loan)).thenReturn(savedLoan);

        ResponseEntity<Map<String, Object>> response = loanController.createLoan(loan);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("loan"));
        assertEquals(savedLoan, body.get("loan"));
        
        verify(loanService, times(1)).createLoan(loan);
    }


    @Test
    void createLoan_ThrowsLoanException_ReturnsBadRequest() {
        Loan loan = new Loan();
        loan.setUserId("U-12345");
        when(loanService.createLoan(loan)).thenThrow(new LoanException("error test"));

        ResponseEntity<Map<String, Object>> response = loanController.createLoan(loan);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertEquals("error test", body.get("error"));

    }

    @Test
    void createLoan_ThrowsArticleException_ReturnsBadRequest() {
        Loan loan = new Loan();
        loan.setUserId("U-12345");
        when(loanService.createLoan(loan)).thenThrow(new ArticleException("article error"));

        ResponseEntity<Map<String, Object>> response = loanController.createLoan(loan);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("article error", body.get("error"));
    }

    // --- deleteLoan ---

    @Test
    void deleteLoan_Success() {
        Loan loan = new Loan();
        loan.setId("LN-123");
        when(loanService.deleteLoanById("LN-123")).thenReturn(loan);

        ResponseEntity<?> response = loanController.deleteLoan("LN-123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(loanService, times(1)).deleteLoanById("LN-123");
    }

    @Test
    void deleteLoan_LoanException_ReturnsBadRequest() {
        doThrow(new LoanException("no se puede eliminar")).when(loanService).deleteLoanById("LN-123");
        ResponseEntity<?> response = loanController.deleteLoan("LN-123");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertEquals("no se puede eliminar", body.get("error"));
    }

    @Test
    void deleteLoan_IllegalArgumentException_ReturnsBadRequest() {
        ResponseEntity<?> response = loanController.deleteLoan("");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
        assertEquals("El ID del préstamo no puede estar vacío", body.get("error"));

    }

    // --- getLoans (varios caminos) ---

    @Test
    void getLoans_ById_Success() {
        Loan loan = new Loan();
        loan.setId("LN-1");
        when(loanService.getLoanById("LN-1")).thenReturn(loan);

        Map<String, Object> result = loanController.getLoans("LN-1", null, null, null, null).getBody();

        assertNotNull(result);
        assertTrue(result.containsKey("loan"));
        assertEquals(loan, result.get("loan"));
    }

    @Test
    void getLoans_ByUser_Success() {
        Loan l1 = new Loan(); l1.setId("LN-1");
        Loan l2 = new Loan(); l2.setId("LN-2");
        List<Loan> loans = Arrays.asList(l1, l2);

        when(loanService.getLoansByUserReport("U-1")).thenReturn(loans);

        Map<String, Object> result = loanController.getLoans(null, "U-1", null, null, null).getBody();

        assertNotNull(result);
        assertTrue(result.containsKey("loans"));
        assertEquals(loans, result.get("loans"));
    }

    @Test
    void getLoans_ByDateRangeAndStatus_Success() {
        Loan l1 = new Loan(); l1.setId("LN-1");
        List<Loan> loans = Collections.singletonList(l1);

        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        when(loanService.getLoansByDateRangeAndStatus(start, end, "Prestado")).thenReturn(loans);

        Map<String, Object> result = loanController.getLoans(null, null, "Prestado", start, end).getBody();

        assertNotNull(result);
        assertEquals(loans, result.get("loans"));
    }

    @Test
    void getLoans_All_Success() {
        List<Loan> loans = Arrays.asList(new Loan(), new Loan());
        when(loanService.getLoans("Prestado")).thenReturn(loans);

        Map<String, Object> result = loanController.getLoans(null, null, "Prestado", null, null).getBody();

        assertNotNull(result);
        assertEquals(loans, result.get("loans"));
    }

    @Test
    void getLoans_LoanException_ReturnsBadRequest() {
        when(loanService.getLoans(any())).thenThrow(new LoanException("error get"));
        ResponseEntity<Map<String, Object>> response = loanController.getLoans(null, null, "X", null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error get", body.get("error"));
    }

    @Test
    void getLoans_IllegalArgumentException_ReturnsBadRequest() {
        // En este caso necesitamos ambas fechas, pero con una relación inválida
        LocalDate start = LocalDate.now().plusDays(5); // Fecha futura
        LocalDate end = LocalDate.now(); // Fecha actual

        ResponseEntity<Map<String, Object>> response = loanController.getLoans(null, null, null, start, end);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("error"));
    }

    // --- updateLoan ---

    @Test
    void updateLoan_Devolver_Success() {
        doNothing().when(loanService).devolverLoan("LN-1");

        Map<String, Object> updates = new HashMap<>();
        updates.put("devolver", true);

        ResponseEntity<Map<String, Object>> response = loanController.updateLoan("LN-1", updates);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Préstamo devuelto", body.get("message"));
        assertEquals("Todos los artículos actualizados según estado del equipo", body.get("details"));
    }

    @Test
    void updateLoan_ArticulosYUpdate_Success() {
        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> articulos = new HashMap<>();
        articulos.put("101", "Disponible");
        updates.put("articulos", articulos);
        updates.put("equipmentStatus", "Dañado");

        doNothing().when(loanService).updateArticlesStatus(eq("LN-1"), any());
        doNothing().when(loanService).updateLoan(eq("LN-1"), any());

        ResponseEntity<Map<String, Object>> response = loanController.updateLoan("LN-1", updates);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Préstamo actualizado", body.get("message"));
        assertTrue(body.containsKey("updated_fields"));
    }

    @Test
    void updateLoan_OnlyUpdate_Success() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("equipmentStatus", "Dañado");

        doNothing().when(loanService).updateLoan(eq("LN-1"), any());

        ResponseEntity<Map<String, Object>> response = loanController.updateLoan("LN-1", updates);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Préstamo actualizado", body.get("message"));
        assertTrue(body.containsKey("updated_fields"));
    }

    @Test
    void updateLoan_LoanException_ReturnsBadRequest() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("equipmentStatus", "Dañado");
        doThrow(new LoanException("error update")).when(loanService).updateLoan(eq("LN-1"), any());

        ResponseEntity<Map<String, Object>> response = loanController.updateLoan("LN-1", updates);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error update", body.get("error"));
    }

    @Test
    void updateLoan_IllegalArgumentException_ReturnsBadRequest() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("equipmentStatus", "Dañado");

        ResponseEntity<Map<String, Object>> response = loanController.updateLoan("", updates);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("El ID del préstamo no puede estar vacío", body.get("error"));
    }

    @Test
    void updateLoan_ClassCastException_ReturnsBadRequest() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("articulos", "notAMap");

        ResponseEntity<Map<String, Object>> response = loanController.updateLoan("LN-1", updates);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.get("error").toString().contains("El campo 'articulos' debe ser un objeto JSON válido"));
    }

    // --- extractArticulosMap ---

    @Test
    void extractArticulosMap_Null_ReturnsEmptyMap() throws Exception {
        Map<String, String> map = invokeExtractArticulosMap(null);
        assertTrue(map.isEmpty());
    }

    @Test
    void extractArticulosMap_Map_Success() throws Exception {
        Map<String, Object> articulos = new HashMap<>();
        articulos.put("101", "Disponible");
        Map<String, String> map = invokeExtractArticulosMap(articulos);
        assertEquals("Disponible", map.get("101"));
    }

    @Test
    void extractArticulosMap_InvalidId_ThrowsException() {
        Map<String, Object> articulos = new HashMap<>();
        articulos.put("notANumber", "Disponible");

        Exception exception = assertThrows(Exception.class, () -> {
            try {
                invokeExtractArticulosMap(articulos);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });

        assertTrue(exception instanceof IllegalArgumentException);
        assertTrue(exception.getMessage().contains("ID de artículo inválido"));
    }

    @Test
    void extractArticulosMap_NotAMap_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            try {
                invokeExtractArticulosMap("noMap");
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });

        assertTrue(exception instanceof IllegalArgumentException);
        assertTrue(exception.getMessage().contains("El campo 'articulos' debe ser un objeto JSON válido"));
    }
    // --- validateUpdatePayload ---

    @Test
    void validateUpdatePayload_Null_Throws() throws Exception {
        assertThrows(NullPointerException.class, () -> invokeValidateUpdatePayload(null));
    }

    @Test
    void validateUpdatePayload_Empty_Throws() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> invokeValidateUpdatePayload(new HashMap<>()));
    }

    // --- validateId ---

    @Test 
    void validateId_NullOrEmpty_Throws() {
        assertThrows(IllegalArgumentException.class, () -> invokeValidateId(null));
        assertThrows(IllegalArgumentException.class, () -> invokeValidateId(""));
        assertThrows(IllegalArgumentException.class, () -> invokeValidateId("  "));
    }


    // --- validateDateRange ---

    @Test 
    void validateDateRange_Null_Throws() {
        LocalDate now = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () -> invokeValidateDateRange(null, now));
        assertThrows(IllegalArgumentException.class, () -> invokeValidateDateRange(now, null));
    }


    @Test
    void validateDateRange_StartAfterEnd_Throws()  {
        LocalDate start = LocalDate.of(2024, 6, 2);
        LocalDate end = LocalDate.of(2024, 6, 1);

        Exception exception = assertThrows(Exception.class, () -> {
            try {
                invokeValidateDateRange(start, end);
            } catch (InvocationTargetException e) {
                // Extraer la causa real de la excepción
                throw e.getCause();
            }
        });

        assertTrue(exception instanceof IllegalArgumentException);
        assertEquals("La fecha de inicio no puede ser posterior a la fecha de fin", exception.getMessage());
    }
    // Reflection helpers for private methods
    @SuppressWarnings("unchecked") 
    private Map<String, String> invokeExtractArticulosMap(Object obj) throws Exception { 
        var method = LoanController.class.getDeclaredMethod("extractArticulosMap", Object.class);
        method.setAccessible(true);
        return (Map<String, String>) method.invoke(loanController, obj);
    }


    private void invokeValidateUpdatePayload(Map<String, Object> map) throws Throwable {
        var method = LoanController.class.getDeclaredMethod("validateUpdatePayload", Map.class);
        method.setAccessible(true);
        try {
            method.invoke(loanController, map);
        } catch (InvocationTargetException e) {
            // Propagar la causa real en lugar de la excepción de reflexión
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
    }

    private void invokeValidateId(String id) throws Throwable {  // Cambia Exception a Throwable
        var method = LoanController.class.getDeclaredMethod("validateId", String.class);
        method.setAccessible(true);
        try {
            method.invoke(loanController, id);
        } catch (InvocationTargetException e) {
            // Extraer y lanzar la causa raíz
            throw e.getCause();
        }
    }

    private void invokeValidateDateRange(LocalDate start, LocalDate end) throws Throwable {
        var method = LoanController.class.getDeclaredMethod("validateDateRange", LocalDate.class, LocalDate.class);
        method.setAccessible(true);
        try {
            method.invoke(loanController, start, end);
        } catch (InvocationTargetException e) {
            // Extraer y lanzar la causa raíz
            throw e.getCause();
        }
    }
}