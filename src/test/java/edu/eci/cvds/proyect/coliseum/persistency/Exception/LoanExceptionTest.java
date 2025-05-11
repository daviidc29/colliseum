package edu.eci.cvds.proyect.coliseum.persistency.Exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class LoanExceptionTest {

    @Test
    void testLoanExceptionBaseClass() {
        String message = "Base LoanException Message";
        LoanException exception = new LoanException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testLoanExceptionTimeError() {
        String message = "Time error occurred";
        LoanException.LoanExceptionTimeError exception =
                new LoanException.LoanExceptionTimeError(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionStateError() {
        String message = "State error occurred";
        LoanException.LoanExceptionStateError exception =
                new LoanException.LoanExceptionStateError(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionEstudianteHasPrestamo() {
        String message = "El estudiante ya tiene un préstamo";
        LoanException.LoanExceptionEstudianteHasPrestamo exception =
                new LoanException.LoanExceptionEstudianteHasPrestamo(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionBookIsAvailable() {
        String message = "El libro está disponible";
        LoanException.LoanExceptionBookIsAvailable exception =
                new LoanException.LoanExceptionBookIsAvailable(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionPrestamoIdNotFound() {
        String message = "No se encontró el ID del préstamo";
        LoanException.LoanExceptionPrestamoIdNotFound exception =
                new LoanException.LoanExceptionPrestamoIdNotFound(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionEstudianteHasNotPrestamo() {
        String message = "El estudiante no tiene préstamos";
        LoanException.LoanExceptionEstudianteHasNotPrestamo exception =
                new LoanException.LoanExceptionEstudianteHasNotPrestamo(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }
}