package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class LoanTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldCreateLoanWithBuilder() {
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
    public void shouldReturnZeroLoanTimeIfDatesMissing() {
        Loan loan = new Loan();
        loan.setLoanDate(null);
        loan.setDevolutionDate(null);

        assertThat(loan.getLoanTime()).isZero();
    }

    @Test
    public void shouldValidateInvalidRole() {
        Loan loan = validLoan();
        loan.setUserRole("Invitado");

        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("userRole"));
    }

    @Test
    public void shouldValidateMissingFields() {
        Loan loan = new Loan(); // all fields null

        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertThat(violations).isNotEmpty();
    }

    @Test
    public void shouldNotAllowEmptyArticleList() {
        Loan loan = validLoan();
        loan.setArticleIds(new ArrayList<>());

        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("articleIds"));
    }

    @Test
    public void shouldRejectTooLongDescription() {
        Loan loan = validLoan();
        loan.setLoanDescriptionType("a".repeat(600));

        Set<ConstraintViolation<Loan>> violations = validator.validate(loan);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("LoanDescriptionType"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Loan loan1 = validLoan();
        Loan loan2 = validLoan();

        assertThat(loan1).isEqualTo(loan2);
        assertThat(loan1.hashCode()).isEqualTo(loan2.hashCode());
    }

    @Test
    public void testToString() {
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
