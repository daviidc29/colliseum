package edu.eci.cvds.proyect.coliseum.persistency.dto;
import lombok.*;

public class DevolutionDto {


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class DevoluctonDto {
        private String userId;
        private String emailGuardian;
        private String bookId;
        private boolean loanReturn;
    }

}
