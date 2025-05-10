package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class LoanTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateLoanWithBuilder() {
        Loan loan = Loan.builder()
                .id("123")
                .articleIds(List.of(1, 2))
                .nameUser("Juan")
                .userId("U001")
                .userRole("Estudiante")
                .LoanDescriptionType("Préstamo para proyecto")
                .creationDate(LocalDateTime.now())
                .loanDate(LocalDate.of(2024, 5, 1))
                .devolutionDate(LocalDate.of(2024, 5, 3))
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .devolutionRsegister("Entregado sin novedad")
                .build();

        assertThat(loan.getNameUser()).isEqualTo("Juan");
        assertThat(loan.getLoanTime()).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroLoanTimeIfDatesMissing() {
        Loan loan = new Loan();
        loan.setLoanDate(null);
        loan.setDevolutionDate(null);

        assertThat(loan.getLoanTime()).isZero();
    }

    @Test
    void shouldValidateInvalidRole() {
        Loan loan = validLoan();
        loan.setUserRole("Invitado");

        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("userRole"));
    }

    @Test
    void shouldValidateMissingFields() {
        Loan loan = new Loan(); // all fields null

        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldNotAllowEmptyArticleList() {
        Loan loan = validLoan();
        loan.setArticleIds(new ArrayList<>());

        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("articleIds"));
    }

    @Test
    void shouldRejectTooLongDescription() {
        Loan loan = validLoan();
        loan.setLoanDescriptionType("a".repeat(600));

        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("LoanDescriptionType"));
    }

    @Test
    void testEqualsAndHashCode() {

        Loan loan1 = Loan.builder()
                .id("123")
                .articleIds(List.of(1, 2))
                .nameUser("Juan")
                .userId("U001")
                .userRole("Estudiante")
                .LoanDescriptionType("Préstamo para proyecto")
                .creationDate(LocalDateTime.now())
                .loanDate(LocalDate.of(2024, 5, 1))
                .devolutionDate(LocalDate.of(2024, 5, 3))
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .devolutionRsegister("Entregado sin novedad")
                .build();

        Loan loan2 = Loan.builder()
                .id("123")
                .articleIds(List.of(1, 2))
                .nameUser("Juan")
                .userId("U001")
                .userRole("Estudiante")
                .LoanDescriptionType("Préstamo para proyecto")
                .creationDate(LocalDateTime.now())
                .loanDate(LocalDate.of(2024, 5, 1))
                .devolutionDate(LocalDate.of(2024, 5, 3))
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .devolutionRsegister("Entregado sin novedad")
                .build();
        assertThat(loan1)
            .isEqualTo(loan2)
            .hasSameHashCodeAs(loan2);
    }




    @Test
    void testToString() {
        Loan loan = validLoan();
        assertThat(loan.toString()).contains("Juan");
    }

    private Loan validLoan() {
        return Loan.builder()
                .id("1")
                .articleIds(List.of(101, 202))
                .nameUser("Juan")
                .userId("U01")
                .userRole("Docente")
                .LoanDescriptionType("Descripción válida")
                .creationDate(LocalDateTime.now())
                .loanDate(LocalDate.of(2024, 5, 1))
                .devolutionDate(LocalDate.of(2024, 5, 2))
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .devolutionRsegister("Todo bien")
                .build();
    }
}
