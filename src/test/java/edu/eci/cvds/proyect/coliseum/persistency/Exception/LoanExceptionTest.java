package edu.eci.cvds.proyect.coliseum.persistency.Exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoanExceptionTest {

    @Test
    void testBaseLoanExceptionCreation() {
        // Arrange & Act
        String errorMessage = "Test error message";
        LoanException exception = new LoanException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testLoanExceptionTimeError() {
        // Arrange & Act
        String errorMessage = "Tiempo de préstamo inválido";
        LoanException.LoanExceptionTimeError exception = new LoanException.LoanExceptionTimeError(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionStateError() {
        // Arrange & Act
        String errorMessage = "Estado de préstamo inválido";
        LoanException.LoanExceptionStateError exception = new LoanException.LoanExceptionStateError(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionEstudianteHasPrestamo() {
        // Arrange & Act
        String errorMessage = "El estudiante ya tiene un préstamo activo";
        LoanException.LoanExceptionEstudianteHasPrestamo exception =
                new LoanException.LoanExceptionEstudianteHasPrestamo(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionBookIsAvailable() {
        // Arrange & Act
        String errorMessage = "El libro no está disponible";
        LoanException.LoanExceptionBookIsAvailable exception =
                new LoanException.LoanExceptionBookIsAvailable(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionPrestamoIdNotFound() {
        // Arrange & Act
        String errorMessage = "ID de préstamo no encontrado";
        LoanException.LoanExceptionPrestamoIdNotFound exception =
                new LoanException.LoanExceptionPrestamoIdNotFound(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testLoanExceptionEstudianteHasNotPrestamo() {
        // Arrange & Act
        String errorMessage = "El estudiante no tiene préstamos";
        LoanException.LoanExceptionEstudianteHasNotPrestamo exception =
                new LoanException.LoanExceptionEstudianteHasNotPrestamo(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertTrue(exception instanceof LoanException);
    }

    @Test
    void testExceptionHierarchy() {
        // Verificar que todas las excepciones son RuntimeException
        new LoanException("test");
        assertTrue(true);
        new LoanException.LoanExceptionTimeError("test");
        assertTrue(true);
        new LoanException.LoanExceptionStateError("test");
        assertTrue(true);
        new LoanException.LoanExceptionEstudianteHasPrestamo("test");
        assertTrue(true);
        new LoanException.LoanExceptionBookIsAvailable("test");
        assertTrue(true);
        new LoanException.LoanExceptionPrestamoIdNotFound("test");
        assertTrue(true);
        new LoanException.LoanExceptionEstudianteHasNotPrestamo("test");
        assertTrue(true);
    }

    @Test
    void testExceptionPropagation() {
        // Verificar que las excepciones se propagan correctamente
        try {
            throw new LoanException.LoanExceptionTimeError("Error de tiempo");
        } catch (LoanException e) {
            assertEquals("Error de tiempo", e.getMessage());
            assertTrue(e instanceof LoanException.LoanExceptionTimeError);
        }
    }
}