package edu.eci.cvds.proyect.coliseum.persistency.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;


@Data               // Genera getters, setters, toString, equals y hashCode
@NoArgsConstructor  // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los campos
@Builder
@Document(collection="article")// Builder pattern

public class Article {
    @Id
    private Integer id;

    @NotBlank(message="El nombre de articulo no puede estar vació")
    @Size(max=500,message="El nombre del articulo no puede tener mas de 500 caracteres")
    private String name;


    @NotBlank(message="El estado del articulo no puede estar vació")
    @Pattern(regexp = "Disponible|Dañado|RequireMantenimiento|Prestado|Devuelto|Perdido", message = "El estado del articulo no es valido")
    private String articleStatus;
    private String description;
    private String imageUrl; 

}

