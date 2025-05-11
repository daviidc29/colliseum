package edu.eci.cvds.proyect.coliseum.persistency.exception;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ValidationExceptionHandlerTest {

    @Test 
    void shouldHandleValidationExceptionsWithMultipleFieldErrors() { 
        // Preparar objetos simulados (mocks)
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("ObjectName", "campo1", "Error en campo1"));
        fieldErrors.add(new FieldError("ObjectName", "campo2", "Error en campo2"));
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Crear un mock o un valor válido para MethodParameter
        MethodParameter mockMethodParameter = Mockito.mock(MethodParameter.class);
        
        // Instanciar la excepción con un parámetro válido
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mockMethodParameter, bindingResult);

        // Instanciar el handler y ejecutar el método
        ValidationExceptionHandler handler = new ValidationExceptionHandler();
        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        // Verificar resultados
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("campo1", "Error en campo1");
        expectedErrors.put("campo2", "Error en campo2");
        assertEquals(expectedErrors, response.getBody());
    }


    @Test 
    void shouldHandleValidationExceptionsWithNoFieldErrors() {
        // Preparar objetos simulados (mocks)
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Crear un mock para MethodParameter
        MethodParameter mockMethodParameter = Mockito.mock(MethodParameter.class);

        // Instanciar la excepción con un MethodParameter válido (no null)
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mockMethodParameter, bindingResult);

        // Instanciar el handler y ejecutar el método
        ValidationExceptionHandler handler = new ValidationExceptionHandler();
        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        // Verificar resultados
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Se espera un mapa vacío cuando no hay errores
        assertEquals(new HashMap<>(), response.getBody());
    }


}