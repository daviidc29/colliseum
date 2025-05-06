package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertTest {

    @Test
    void getIdTest() {
        Alert alert = new Alert("1", "Falla en equipo", "El proyector no enciende", LocalDateTime.now());
        assertEquals("1", alert.getId());
    }

    @Test
    void getDescriptionTest() {
        Alert alert = new Alert("2", "Reserva rechazada", "No hay artículos disponibles", LocalDateTime.now());
        assertEquals("Reserva rechazada", alert.getDescription());
    }

    @Test
    void getMessageTest() {
        Alert alert = new Alert("3", "Alerta general", "Artículo perdido", LocalDateTime.now());
        assertEquals("Artículo perdido", alert.getMessage());
    }

    @Test
    void getTimestampTest() {
        LocalDateTime now = LocalDateTime.of(2025, 5, 5, 10, 0);
        Alert alert = new Alert("4", "Tiempo agotado", "Artículo no devuelto", now);
        assertEquals(now, alert.getTimestamp());
    }

    @Test
    void setIdTest() {
        Alert alert = new Alert(null, null, null, null);
        alert.setId("100");
        assertEquals("100", alert.getId());
    }

    @Test
    void setDescriptionTest() {
        Alert alert = new Alert(null, null, null, null);
        alert.setDescription("Nuevo aviso");
        assertEquals("Nuevo aviso", alert.getDescription());
    }

    @Test
    void setMessageTest() {
        Alert alert = new Alert(null, null, null, null);
        alert.setMessage("Mensaje actualizado");
        assertEquals("Mensaje actualizado", alert.getMessage());
    }

    @Test
    void setTimestampTest() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 30);
        Alert alert = new Alert(null, null, null, null);
        alert.setTimestamp(now);
        assertEquals(now, alert.getTimestamp());
    }
}