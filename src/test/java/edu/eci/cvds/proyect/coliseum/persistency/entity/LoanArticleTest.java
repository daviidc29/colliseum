package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;

class LoanArticleTest {

    private LoanArticle emptyLoanArticle;
    private LoanArticle fullLoanArticle;

    @BeforeEach
    void setUp() {
        emptyLoanArticle = new LoanArticle();

        // Initialize with proper values including all fields from the entity
        fullLoanArticle = new LoanArticle(
                "loan123",
                Arrays.asList(1, 2, 3),
                "John Doe",
                "user123",
                "Estudiante",
                "Préstamo de materiales deportivo",
                LocalDateTime.of(2025, 5, 10, 10, 30),
                LocalDate.of(2025, 5, 10),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 5, 15),
                "Prestado",
                "En buen estado",
                "Ningún motivo de devolución"
        );
    }

    @Test
    void testEmptyConstructor() {
        assertNotNull(emptyLoanArticle);
        assertNull(emptyLoanArticle.getId());
        assertNull(emptyLoanArticle.getArticleIds());
        assertNull(emptyLoanArticle.getNameUser());
        assertNull(emptyLoanArticle.getUserId());
        assertNull(emptyLoanArticle.getUserRole());
        assertNull(emptyLoanArticle.getLoanDescriptionType());
        assertNull(emptyLoanArticle.getCreationDate());
        assertNull(emptyLoanArticle.getLoanDate());
        assertNull(emptyLoanArticle.getStartTime());
        assertNull(emptyLoanArticle.getEndTime());
        assertNull(emptyLoanArticle.getDevolutionDate());
        assertNull(emptyLoanArticle.getLoanStatus());
        assertNull(emptyLoanArticle.getEquipmentStatus());
        assertNull(emptyLoanArticle.getDevolutionRsegister());
    }

    @Test
    void testAllArgsConstructor() {
        assertEquals("loan123", fullLoanArticle.getId());
        assertEquals(3, fullLoanArticle.getArticleIds().size());
        assertEquals("John Doe", fullLoanArticle.getNameUser());
        assertEquals("user123", fullLoanArticle.getUserId());
        assertEquals("Estudiante", fullLoanArticle.getUserRole());
        assertEquals("Préstamo de materiales deportivo", fullLoanArticle.getLoanDescriptionType());
        assertNotNull(fullLoanArticle.getCreationDate());
        assertEquals(LocalDate.of(2025, 5, 10), fullLoanArticle.getLoanDate());
        assertEquals(LocalTime.of(10, 0), fullLoanArticle.getStartTime());
        assertEquals(LocalTime.of(12, 0), fullLoanArticle.getEndTime());
        assertEquals(LocalDate.of(2025, 5, 15), fullLoanArticle.getDevolutionDate());
        assertEquals("Prestado", fullLoanArticle.getLoanStatus());
        assertEquals("En buen estado", fullLoanArticle.getEquipmentStatus());
        assertEquals("Ningún motivo de devolución", fullLoanArticle.getDevolutionRsegister());
    }

    @Test
    void testSettersAndGetters() {
        emptyLoanArticle.setId("myLoan");
        emptyLoanArticle.setArticleIds(Collections.singletonList(99));
        emptyLoanArticle.setNameUser("Jane");
        emptyLoanArticle.setUserId("user456");
        emptyLoanArticle.setUserRole("Docente");
        emptyLoanArticle.setLoanStatus("Devuelto");
        emptyLoanArticle.setEquipmentStatus("Danado");
        emptyLoanArticle.setLoanDescriptionType("Prestamo de emergencia");
        emptyLoanArticle.setDevolutionRsegister("Motivo de prueba");
        LocalDateTime now = LocalDateTime.now();
        emptyLoanArticle.setCreationDate(now);
        LocalDate date1 = LocalDate.of(2025, 5, 20);
        LocalDate date2 = LocalDate.of(2025, 6, 1);
        emptyLoanArticle.setLoanDate(date1);
        emptyLoanArticle.setDevolutionDate(date2);

        // Test time fields
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        emptyLoanArticle.setStartTime(startTime);
        emptyLoanArticle.setEndTime(endTime);

        assertEquals("myLoan", emptyLoanArticle.getId());
        assertEquals(1, emptyLoanArticle.getArticleIds().size());
        assertEquals("Jane", emptyLoanArticle.getNameUser());
        assertEquals("user456", emptyLoanArticle.getUserId());
        assertEquals("Docente", emptyLoanArticle.getUserRole());
        assertEquals("Devuelto", emptyLoanArticle.getLoanStatus());
        assertEquals("Danado", emptyLoanArticle.getEquipmentStatus());
        assertEquals("Prestamo de emergencia", emptyLoanArticle.getLoanDescriptionType());
        assertEquals("Motivo de prueba", emptyLoanArticle.getDevolutionRsegister());
        assertEquals(now, emptyLoanArticle.getCreationDate());
        assertEquals(date1, emptyLoanArticle.getLoanDate());
        assertEquals(date2, emptyLoanArticle.getDevolutionDate());
        assertEquals(startTime, emptyLoanArticle.getStartTime());
        assertEquals(endTime, emptyLoanArticle.getEndTime());
    }

    @Test
    void testGetLoanTime() {
        // Full loan date is 2025-05-10 to 2025-05-15, difference is 5 days
        assertEquals(5, fullLoanArticle.getLoanTime());

        // If either date is null, result is 0
        emptyLoanArticle.setLoanDate(null);
        emptyLoanArticle.setDevolutionDate(LocalDate.of(2025, 5, 15));
        assertEquals(0, emptyLoanArticle.getLoanTime());
    }

    @Test
    void testGetDuration() {
        // Duration between 10:00 and 12:00 should be 2 hours
        Duration expected = Duration.ofHours(2);
        assertEquals(expected, fullLoanArticle.getDuration());

        // Test with null times
        emptyLoanArticle.setStartTime(null);
        emptyLoanArticle.setEndTime(LocalTime.of(14, 0));
        assertNull(emptyLoanArticle.getDuration());

        emptyLoanArticle.setStartTime(LocalTime.of(10, 0));
        emptyLoanArticle.setEndTime(null);
        assertNull(emptyLoanArticle.getDuration());
    }

    @Test
    void testEqualsAndHashCode() {
        LoanArticle sameLoanArticle = new LoanArticle(
                "loan123",
                Arrays.asList(1, 2, 3),
                "John Doe",
                "user123",
                "Estudiante",
                "Préstamo de materiales deportivo",
                LocalDateTime.of(2025, 5, 10, 10, 30),
                LocalDate.of(2025, 5, 10),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 5, 15),
                "Prestado",
                "En buen estado",
                "Ningún motivo de devolución"
        );

        // Should be equal
        assertEquals(fullLoanArticle, sameLoanArticle);
        assertEquals(fullLoanArticle.hashCode(), sameLoanArticle.hashCode());

        // Should not be equal
        LoanArticle differentLoanArticle = new LoanArticle();
        differentLoanArticle.setId("anotherId");
        assertNotEquals(fullLoanArticle, differentLoanArticle);
        assertNotEquals(fullLoanArticle.hashCode(), differentLoanArticle.hashCode());
    }

    @Test
    void testCanEqual() {
        Object obj = new Object();
        assertFalse(fullLoanArticle.canEqual(obj));
        assertTrue(fullLoanArticle.canEqual(new LoanArticle()));
    }

    @Test
    void testToString() {
        String str = fullLoanArticle.toString();
        assertTrue(str.contains("loan123"));
        assertTrue(str.contains("John Doe"));
        assertTrue(str.contains("Estudiante"));
        // Check for time fields too
        assertTrue(str.contains("startTime"));
        assertTrue(str.contains("endTime"));
    }

    @Test
    void testBuilder() {
        LoanArticle builtLoan = LoanArticle.builder()
                .id("builder123")
                .nameUser("Builder User")
                .userId("builder456")
                .userRole("Docente")
                .loanStatus("Prestado")
                .build();

        assertNotNull(builtLoan);
        assertEquals("builder123", builtLoan.getId());
        assertEquals("Builder User", builtLoan.getNameUser());
        assertEquals("builder456", builtLoan.getUserId());
        assertEquals("Docente", builtLoan.getUserRole());
        assertEquals("Prestado", builtLoan.getLoanStatus());
    }
}