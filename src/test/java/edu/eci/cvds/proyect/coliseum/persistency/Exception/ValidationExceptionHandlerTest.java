package edu.eci.cvds.proyect.coliseum.persistency.Exception;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ValidationExceptionHandlerTest {

    @InjectMocks
    private ValidationExceptionHandler validationExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    public ValidationExceptionHandlerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleValidationExceptions_ReturnsBadRequestAndFieldErrors() {
        // Arrange
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("TestObject", "field1", "Error message 1"),
                new FieldError("TestObject", "field2", "Error message 2")
        );

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        ResponseEntity<Map<String, String>> response =
                validationExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, String> body = response.getBody();
        Map<String, String> expected = new HashMap<>();
        expected.put("field1", "Error message 1");
        expected.put("field2", "Error message 2");

        assertEquals(expected, body);
    }
}