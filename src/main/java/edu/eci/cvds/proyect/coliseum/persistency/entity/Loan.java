package edu.eci.cvds.proyect.coliseum.persistency.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data               // Genera getters, setters, toString, equals y hashCode
@Getter
@Setter
@NoArgsConstructor  // Constructor sin argumentos
@AllArgsConstructor // Constructor con todos los campos
@Builder
@Document(collection="loans")// Builder pattern

public class Loan {
    @Id
    private String id;

    @NotEmpty(message = "Debe incluir al menos un artículo")
    private List<Integer> articleIds;

    @NotBlank(message="El nombre de usuario no puede estar vació")
    @NonNull
    private String nameUser;

    @NotBlank(message="El id del usuario no puede estar vació")
    private String userId;

    @NotBlank(message="El rol del usuario no puede estar vació")
    @Pattern(regexp = "Estudiante|Docente|Administrativo|ServiciosGenerales", message = "El rol del usuario no es valido")
    private String userRole;

    @NotBlank(message="La descripcion y tipo de prestamo no puede estar vacías")
    @Size(max=500,message="La descripcion y tipo de prestamo no puede tener mas de 500 caracteres")
    private String LoanDescriptionType;

    @CreatedDate
    private LocalDateTime creationDate;

    private LocalDate loanDate;

    private LocalDate devolutionDate;

    @NotBlank(message="El estado del prestamo no puede estar vacío")
    @Pattern(regexp="Prestado|Vencido|Devuelto",message="El estado del prestamo no es valido")
    private String loanStatus;

    @NotBlank(message="El estado del equipo prestado no puede estar vacío")
    @Pattern(regexp="En buen estado|Dañado| Requiere mantenimiento",message = "El estado del equipo prestado no es valido")
    private String equipmentStatus;

    @Size(max=500,message="El motivo de la devolucion no puede tener mas de 500 caracteres")
    private String devolutionRsegister;


    public long getLoanTime() {
        if(loanDate!=null&&devolutionDate!=null){
           return java.time.temporal.ChronoUnit.DAYS.between(loanDate, devolutionDate);
        }
        return 0;
    }
}