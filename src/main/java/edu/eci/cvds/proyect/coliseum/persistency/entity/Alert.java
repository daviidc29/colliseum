package edu.eci.cvds.proyect.coliseum.persistency.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "alerts")
public class Alert {
    @Id
    private String id;
    private String description;
    private String message;
    private LocalDateTime timestamp;
    public Alert(String id, String description, String message, LocalDateTime timestamp) {
        this.id = id;
        this.description = description;
        this.message = message;
        this.timestamp = timestamp;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


}
