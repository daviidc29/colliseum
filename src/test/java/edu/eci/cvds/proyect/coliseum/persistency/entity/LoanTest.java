package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

class LoanTest {

    private Loan emptyLoan;
    private Loan fullLoan;

    @BeforeEach
    void setUp() {
        emptyLoan = new Loan();

        fullLoan = new Loan(
                "loan123",
                Arrays.asList(1, 2, 3),
                "John Doe",
                "user123",
                "Estudiante",
                "Préstamo de materiales deportivo",
                LocalDateTime.of(2025, 5, 10, 10, 30),
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 15),
                "Prestado",
                "En buen estado",
                "Ningún motivo de devolución"
        );
    }

    @Test
    void testConstructor() {
        assertNotNull(emptyLoan);
        assertNull(emptyLoan.getId());
        assertNull(emptyLoan.getArticleIds());
        assertNull(emptyLoan.getNameUser());
        assertNull(emptyLoan.getUserId());
        assertNull(emptyLoan.getUserRole());
        assertNull(emptyLoan.getLoanDescriptionType());
        assertNull(emptyLoan.getCreationDate());
        assertNull(emptyLoan.getLoanDate());
        assertNull(emptyLoan.getDevolutionDate());
        assertNull(emptyLoan.getLoanStatus());
        assertNull(emptyLoan.getEquipmentStatus());
        assertNull(emptyLoan.getDevolutionRsegister());

        assertEquals("loan123", fullLoan.getId());
        assertEquals(3, fullLoan.getArticleIds().size());
    }

    @Test
    void testSettersAndGetters() {
        emptyLoan.setId("myLoan");
        emptyLoan.setArticleIds(Collections.singletonList(99));
        emptyLoan.setNameUser("Jane");
        emptyLoan.setUserId("user456");
        emptyLoan.setUserRole("Docente");
        emptyLoan.setLoanStatus("Devuelto");
        emptyLoan.setEquipmentStatus("Dañado");
        emptyLoan.setLoanDescriptionType("Prestamo de emergencia");
        emptyLoan.setDevolutionRsegister("Motivo de prueba");
        LocalDateTime now = LocalDateTime.now();
        emptyLoan.setCreationDate(now);
        LocalDate date1 = LocalDate.of(2025, 5, 20);
        LocalDate date2 = LocalDate.of(2025, 6, 1);
        emptyLoan.setLoanDate(date1);
        emptyLoan.setDevolutionDate(date2);

        assertEquals("myLoan", emptyLoan.getId());
        assertEquals(1, emptyLoan.getArticleIds().size());
        assertEquals("Jane", emptyLoan.getNameUser());
        assertEquals("user456", emptyLoan.getUserId());
        assertEquals("Docente", emptyLoan.getUserRole());
        assertEquals("Devuelto", emptyLoan.getLoanStatus());
        assertEquals("Dañado", emptyLoan.getEquipmentStatus());
        assertEquals("Prestamo de emergencia", emptyLoan.getLoanDescriptionType());
        assertEquals("Motivo de prueba", emptyLoan.getDevolutionRsegister());
        assertEquals(now, emptyLoan.getCreationDate());
        assertEquals(date1, emptyLoan.getLoanDate());
        assertEquals(date2, emptyLoan.getDevolutionDate());
    }

    @Test
    void testGetLoanTime() {
        // Full loan date is 2025-05-10 to 2025-05-15, difference is 5 days
        assertEquals(5, fullLoan.getLoanTime());

        // If either date is null, result is 0
        emptyLoan.setLoanDate(null);
        emptyLoan.setDevolutionDate(LocalDate.of(2025, 5, 15));
        assertEquals(0, emptyLoan.getLoanTime());
    }

    @Test
    void testEqualsAndHashCode() {
        Loan sameLoan = new Loan(
                "loan123",
                Arrays.asList(1, 2, 3),
                "John Doe",
                "user123",
                "Estudiante",
                "Préstamo de materiales deportivo",
                LocalDateTime.of(2025, 5, 10, 10, 30),
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 15),
                "Prestado",
                "En buen estado",
                "Ningún motivo de devolución"
        );

        // Should be equal
        assertEquals(fullLoan, sameLoan);
        assertEquals(fullLoan.hashCode(), sameLoan.hashCode());

        // Should not be equal
        Loan differentLoan = new Loan();
        differentLoan.setId("anotherId");
        assertNotEquals(fullLoan, differentLoan);
        assertNotEquals(fullLoan.hashCode(), differentLoan.hashCode());
    }

    @Test
    void testCanEqual() {
        Object obj = new Object();
        assertFalse(fullLoan.canEqual(obj));
        assertTrue(fullLoan.canEqual(new Loan()));
    }

    @Test
    void testToString() {
        String str = fullLoan.toString();
        assertTrue(str.contains("loan123"));
        assertTrue(str.contains("John Doe"));
    }

    @Test
    void testSetNameUserCoverage() {
        fullLoan.setNameUser("New Name");
        assertEquals("New Name", fullLoan.getNameUser());
    }
}