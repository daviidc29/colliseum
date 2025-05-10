package edu.eci.cvds.proyect.coliseum.persistency.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    private Loan loan;

    @BeforeEach
     void setUp() {
        loan = new Loan();
        loan.setId("loan1");
        loan.setUserId("user123");
        loan.setArticleIds(List.of(1, 2));
        loan.setLoanStatus("Prestado");
        loan.setLoanDate(LocalDate.now());
    }

    @Test
    void testCreateLoanSuccess() throws Exception {
        // Configurar ObjectMapper para manejar fechas
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Crear un préstamo válido (igual al mock)
        Loan loan1 = new Loan();
        loan1.setId("loan1");
        loan1.setArticleIds(List.of(1, 2));
        loan1.setUserId("user123");
        loan1.setNameUser("Nombre Usuario");
        loan1.setUserRole("Estudiante");
        loan1.setLoanDate(LocalDate.now());
        loan1.setLoanStatus("Prestado");
        loan1.setLoanDescriptionType("Descripción válida");
        loan1.setEquipmentStatus("En buen estado");
        // Asegurar que los campos no nulos estén presentes
        loan1.setCreationDate(LocalDateTime.now()); // Si es necesario según la lógica de la aplicación

        // Mock del servicio
        Mockito.when(loanService.createLoan(any(Loan.class))).thenReturn(loan1);

        // Ejecutar la solicitud
        mockMvc.perform(post("/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loan1)))
                .andExpect(status().isCreated())
                // Ajustar la ruta del JSON para coincidir con la estructura de la respuesta
                .andExpect(jsonPath("$.loan.id").value("loan1"))
                // Verificar otros campos relevantes si es necesario
                .andExpect(jsonPath("$.loan.userId").value("user123"))
                .andExpect(jsonPath("$.loan.loanStatus").value("Prestado"));
    }

    @Test
    void testCreateLoanFail() throws Exception {
        // 1. Configurar ObjectMapper
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. Crear un Loan válido (para que no falle en validaciones automáticas)
        Loan loan1 = new Loan();
        loan1.setId("loan1");
        loan1.setArticleIds(List.of(1, 2));
        loan1.setUserId("user123");
        loan1.setNameUser("Nombre Usuario");
        loan1.setUserRole("Estudiante"); // ✔️ Valor permitido por el @Pattern
        loan1.setLoanDate(LocalDate.now());
        loan1.setLoanStatus("Prestado");
        loan1.setLoanDescriptionType("Descripción válida"); // ✔️ Corregido campo
        loan1.setEquipmentStatus("En buen estado"); // ✔️ Valor permitido

        // 3. Mockear el servicio para lanzar excepción
        Mockito.when(loanService.createLoan(any(Loan.class)))
                .thenThrow(new LoanException("Error al crear préstamo"));

        // 4. Ejecutar prueba
        mockMvc.perform(post("/loan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loan1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error al crear préstamo"));
    }


    @Test
    void testDeleteLoanSuccess() throws Exception {
        mockMvc.perform(delete("/loan/loan1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteLoanFail() throws Exception {
        Mockito.doThrow(new LoanException("No se puede eliminar"))
                .when(loanService).deleteLoanById("loan1");

        mockMvc.perform(delete("/loan/loan1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No se puede eliminar"));
    }

    @Test
    void testGetLoanById() throws Exception {
        Mockito.when(loanService.getLoanById("loan1")).thenReturn(loan);

        mockMvc.perform(get("/loan?id=loan1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loan.id").value("loan1"));
    }

    @Test
    void testGetLoanByUser() throws Exception {
        Mockito.when(loanService.getLoansByUserReport("user123")).thenReturn(List.of(loan));

        mockMvc.perform(get("/loan?userId=user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loans[0].id").value("loan1"));
    }

    @Test
    void testGetLoanByDateRange() throws Exception {
        Mockito.when(loanService.getLoansByDateRangeAndStatus(any(), any(), any())).thenReturn(List.of(loan));

        mockMvc.perform(get("/loan?startDate=2024-05-01&endDate=2024-05-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loans[0].id").value("loan1"));
    }

    @Test
    void testGetAllLoans() throws Exception {
        // Configurar el mock para aceptar cualquier String (incluyendo null)
        Mockito.when(loanService.getLoans(Mockito.nullable(String.class))).thenReturn(List.of(loan));

        mockMvc.perform(get("/loan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loans[0].id").value("loan1")); // Asegurar que el JSON path es correcto
    }

    @Test
    void testUpdateLoanOnlyLoanFields() throws Exception {
        mockMvc.perform(patch("/loan/loan1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(Map.of("status", "Devuelto"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Préstamo actualizado"));
    }

    @Test
    void testUpdateLoanWithReturn() throws Exception {
        mockMvc.perform(patch("/loan/loan1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(Map.of("devolver", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Préstamo devuelto"));
    }

    @Test
    void testUpdateLoanWithArticles() throws Exception {
        mockMvc.perform(patch("/loan/loan1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(Map.of("articulos", Map.of("1", "Dañado")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Préstamo actualizado"));
    }

    @Test
    void testUpdateLoanError() throws Exception {
        Mockito.doThrow(new LoanException("Error actualización")).when(loanService).updateLoan(eq("loan1"), anyMap());

        mockMvc.perform(patch("/loan/loan1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(Map.of("status", "X"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error actualización"));
    }
}
