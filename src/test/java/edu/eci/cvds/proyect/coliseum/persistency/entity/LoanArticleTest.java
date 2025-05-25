package edu.eci.cvds.proyect.coliseum.persistency.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoanArticleTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("Test constructor and getters/setters")
    void testConstructorAndGettersSetters() {
        // Create a LoanArticle with the constructor
        LoanArticle loanArticle = new LoanArticle();

        // Set values using setters
        String id = "1";
        List<Integer> articleIds = Arrays.asList(1, 2, 3);
        String nameUser = "Juan Perez";
        String userId = "12345";
        String userRole = "Estudiante";
        String loanDescriptionType = "Préstamo para proyecto final";
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDate loanDate = LocalDate.now();
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(12, 0);
        LocalDate devolutionDate = loanDate.plusDays(7);
        String loanStatus = "Prestado";
        String equipmentStatus = "En buen estado";
        String devolutionRsegister = "Devuelto en perfectas condiciones";

        loanArticle.setId(id);
        loanArticle.setArticleIds(articleIds);
        loanArticle.setNameUser(nameUser);
        loanArticle.setUserId(userId);
        loanArticle.setUserRole(userRole);
        loanArticle.setLoanDescriptionType(loanDescriptionType);
        loanArticle.setCreationDate(creationDate);
        loanArticle.setLoanDate(loanDate);
        loanArticle.setStartTime(startTime);
        loanArticle.setEndTime(endTime);
        loanArticle.setDevolutionDate(devolutionDate);
        loanArticle.setLoanStatus(loanStatus);
        loanArticle.setEquipmentStatus(equipmentStatus);
        loanArticle.setDevolutionRsegister(devolutionRsegister);

        // Test the getters
        assertEquals(id, loanArticle.getId());
        assertEquals(articleIds, loanArticle.getArticleIds());
        assertEquals(nameUser, loanArticle.getNameUser());
        assertEquals(userId, loanArticle.getUserId());
        assertEquals(userRole, loanArticle.getUserRole());
        assertEquals(loanDescriptionType, loanArticle.getLoanDescriptionType());
        assertEquals(creationDate, loanArticle.getCreationDate());
        assertEquals(loanDate, loanArticle.getLoanDate());
        assertEquals(startTime, loanArticle.getStartTime());
        assertEquals(endTime, loanArticle.getEndTime());
        assertEquals(devolutionDate, loanArticle.getDevolutionDate());
        assertEquals(loanStatus, loanArticle.getLoanStatus());
        assertEquals(equipmentStatus, loanArticle.getEquipmentStatus());
        assertEquals(devolutionRsegister, loanArticle.getDevolutionRsegister());
    }

    @Test
    @DisplayName("Test builder pattern")
    void testBuilder() {
        // Use the builder pattern to create a LoanArticle
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(12, 0);

        LoanArticle loanArticle = LoanArticle.builder()
                .id("1")
                .articleIds(Arrays.asList(1, 2, 3))
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("Estudiante")
                .loanDescriptionType("Préstamo para proyecto final")
                .creationDate(now)
                .loanDate(today)
                .startTime(start)
                .endTime(end)
                .devolutionDate(today.plusDays(7))
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .devolutionRsegister("Devuelto en perfectas condiciones")
                .build();

        // Verify the object was created correctly
        assertNotNull(loanArticle);
        assertEquals("1", loanArticle.getId());
        assertEquals(Arrays.asList(1, 2, 3), loanArticle.getArticleIds());
        assertEquals("Juan Perez", loanArticle.getNameUser());
        assertEquals("Estudiante", loanArticle.getUserRole());
        assertEquals(now, loanArticle.getCreationDate());
        assertEquals(start, loanArticle.getStartTime());
        assertEquals(end, loanArticle.getEndTime());
    }

    @Test
    @DisplayName("Test getDuration method")
    void testGetDuration() {
        // Create a loan with start and end times
        LoanArticle loanWithTimes = LoanArticle.builder()
                .nameUser("Juan Perez") // Valor para el campo @NonNull
                .userId("12345") // Campo requerido
                .userRole("Estudiante") // Campo requerido
                .articleIds(Arrays.asList(1, 2)) // Campo requerido
                .loanDescriptionType("Descripción") // Campo requerido
                .loanStatus("Prestado") // Campo requerido
                .equipmentStatus("En buen estado") // Campo requerido
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        // Test duration is calculated correctly (2 hours)
        Duration expectedDuration = Duration.ofHours(2);
        assertEquals(expectedDuration, loanWithTimes.getDuration());

        // Create a loan without times
        LoanArticle loanWithoutTimes = new LoanArticle();
        loanWithoutTimes.setNameUser("Juan Perez"); // Asignar un valor al campo @NonNull

        // Test that duration is null when times are not set
        assertNull(loanWithoutTimes.getDuration());

        // Test with only start time (should return null)
        LoanArticle loanWithOnlyStartTime = LoanArticle.builder()
                .nameUser("Juan Perez") // Valor para el campo @NonNull
                .userId("12345") // Campo requerido
                .userRole("Estudiante") // Campo requerido
                .articleIds(Arrays.asList(1, 2)) // Campo requerido
                .loanDescriptionType("Descripción") // Campo requerido
                .loanStatus("Prestado") // Campo requerido
                .equipmentStatus("En buen estado") // Campo requerido
                .startTime(LocalTime.of(10, 0))
                .build();
        assertNull(loanWithOnlyStartTime.getDuration());

        // Test with only end time (should return null)
        LoanArticle loanWithOnlyEndTime = LoanArticle.builder()
                .nameUser("Juan Perez") // Valor para el campo @NonNull
                .userId("12345") // Campo requerido
                .userRole("Estudiante") // Campo requerido
                .articleIds(Arrays.asList(1, 2)) // Campo requerido
                .loanDescriptionType("Descripción") // Campo requerido
                .loanStatus("Prestado") // Campo requerido
                .equipmentStatus("En buen estado") // Campo requerido
                .endTime(LocalTime.of(12, 0))
                .build();
        assertNull(loanWithOnlyEndTime.getDuration());
    }

    @Test
    @DisplayName("Test getLoanTime method for hourly loans")
    void testGetLoanTimeForHourlyLoans() {
        // Create a loan with start and end times
        LoanArticle hourlyLoan = LoanArticle.builder()
                .nameUser("Juan Cely")          // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Préstamo por horas") // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        // Test loan time is calculated correctly (4 hours)
        assertEquals(4, hourlyLoan.getLoanTime());

        // Test with different time span
        LoanArticle shortLoan = LoanArticle.builder()
                .nameUser("Juan Cely")          // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Préstamo corto") // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .startTime(LocalTime.of(14, 30))
                .endTime(LocalTime.of(15, 30))
                .build();

        // Should be 1 hour
        assertEquals(1, shortLoan.getLoanTime());
    }

    @Test
    @DisplayName("Test getLoanTime method for daily loans")
    void testGetLoanTimeForDailyLoans() {
        // Create a loan with loan date and devolution date (7 days)
        LocalDate loanDate = LocalDate.of(2025, 5, 1);
        LocalDate devolutionDate = LocalDate.of(2025, 5, 8);

        LoanArticle dailyLoan = LoanArticle.builder()
                .nameUser("Juan Perez")         // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Préstamo semanal")   // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .loanDate(loanDate)
                .devolutionDate(devolutionDate)
                .build();

        // Test loan time is calculated correctly (7 days)
        assertEquals(7, dailyLoan.getLoanTime());

        // Test with a 1-day loan
        LoanArticle oneDayLoan = LoanArticle.builder()
                .nameUser("Juan Perez")         // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Préstamo corto")   // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .loanDate(LocalDate.of(2025, 5, 1))
                .devolutionDate(LocalDate.of(2025, 5, 2))
                .build();

        // Should be 1 day
        assertEquals(1, oneDayLoan.getLoanTime());

        // Test with same day loan (0 days)
        LoanArticle sameDayLoan = LoanArticle.builder()
                .nameUser("Juan Perez")         // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Préstamo mismo día")   // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .loanDate(LocalDate.of(2025, 5, 1))
                .devolutionDate(LocalDate.of(2025, 5, 1))
                .build();

        // Should be 0 days
        assertEquals(0, sameDayLoan.getLoanTime());
    }

    @Test
    @DisplayName("Test getLoanTime method with mixed date and time configurations")
    void testGetLoanTimeWithMixedConfigurations() {
        // Create a loan with both dates and times
        LoanArticle mixedLoan = LoanArticle.builder()
                .nameUser("Juan Cely")          // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Préstamo mixto con fechas y horas") // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .loanDate(LocalDate.of(2025, 5, 1))
                .devolutionDate(LocalDate.of(2025, 5, 8))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        // The method should prioritize times over dates, so should return 4 hours
        assertEquals(4, mixedLoan.getLoanTime());
    }

    @Test
    @DisplayName("Test getLoanTime method with no dates or times")
    void testGetLoanTimeWithNoDateOrTime() {
        // Create a loan without dates or times
        LoanArticle emptyLoan = new LoanArticle();
        emptyLoan.setNameUser("Juan Cely"); // Necesario para evitar NullPointerException

        // Test loan time is 0 when no dates or times are set
        assertEquals(0, emptyLoan.getLoanTime());

        // Test with only loanDate set
        LoanArticle onlyLoanDate = LoanArticle.builder()
                .nameUser("Juan Cely")          // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Préstamo con fecha inicial") // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .loanDate(LocalDate.of(2025, 5, 1))
                .build();

        assertEquals(0, onlyLoanDate.getLoanTime());

        // Test with only devolutionDate set
        LoanArticle onlyDevolutionDate = LoanArticle.builder()
                .nameUser("Juan Cely")          // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Préstamo con fecha devolución") // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .devolutionDate(LocalDate.of(2025, 5, 8))
                .build();

        assertEquals(0, onlyDevolutionDate.getLoanTime());
    }

    @Test
    @DisplayName("Test isHourlyLoan method")
    void testIsHourlyLoan() {
        // Create a loan with start and end times
        LoanArticle hourlyLoan = LoanArticle.builder()
                .nameUser("Juan Perez")         // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Descripción")   // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        // Test that it's identified as an hourly loan
        assertTrue(hourlyLoan.isHourlyLoan());

        // Create a loan with only start time
        LoanArticle partialLoan = LoanArticle.builder()
                .nameUser("Juan Perez")         // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Descripción")   // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .startTime(LocalTime.of(9, 0))
                .build();

        // Test that it's not identified as an hourly loan
        assertFalse(partialLoan.isHourlyLoan());

        // Create a loan without times
        LoanArticle dailyLoan = LoanArticle.builder()
                .nameUser("Juan Perez")         // Campo obligatorio (@NonNull)
                .userId("12345")                // Campo obligatorio (@NotBlank)
                .userRole("Estudiante")         // Campo obligatorio (@NotBlank y @Pattern)
                .articleIds(Arrays.asList(1, 2))// Campo obligatorio (@NotEmpty)
                .loanDescriptionType("Descripción")   // Campo obligatorio (@NotBlank)
                .loanStatus("Prestado")         // Campo obligatorio (@NotBlank y @Pattern)
                .equipmentStatus("En buen estado") // Campo obligatorio (@NotBlank y @Pattern)
                .loanDate(LocalDate.now())
                .devolutionDate(LocalDate.now().plusDays(7))
                .build();

        // Test that it's not identified as an hourly loan
        assertFalse(dailyLoan.isHourlyLoan());
    }

    @Test
    @DisplayName("Test validation for articleIds")
    void testValidationForArticleIds() {
        // Create a loan without articleIds
        LoanArticle loan = LoanArticle.builder()
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("Estudiante")
                .loanDescriptionType("Description")
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .build();

        // Validate the loan
        Set<ConstraintViolation<LoanArticle>> violations = validator.validate(loan);

        // Check that there is a violation for articleIds
        assertFalse(violations.isEmpty());
        boolean hasArticleIdsViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("articleIds"));
        assertTrue(hasArticleIdsViolation);

        // Fix the violation
        loan.setArticleIds(Arrays.asList(1, 2, 3));

        // Check that there's no longer a violation for articleIds
        violations = validator.validate(loan);
        hasArticleIdsViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("articleIds"));
        assertFalse(hasArticleIdsViolation);

        // Test with empty list (should still violate @NotEmpty)
        loan.setArticleIds(Collections.emptyList());
        violations = validator.validate(loan);
        hasArticleIdsViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("articleIds"));
        assertTrue(hasArticleIdsViolation);
    }

    @Test
    @DisplayName("Test validation for required fields")
    void testValidationForRequiredFields() {
        // Create a minimal loan with only articleIds using constructor sin argumentos
        // en lugar del builder para evitar la validación @NonNull de Lombok
        LoanArticle loan = new LoanArticle();
        loan.setArticleIds(Arrays.asList(1, 2, 3));

        // Validate the loan
        Set<ConstraintViolation<LoanArticle>> violations = validator.validate(loan);

        // Check that there are violations for required fields
        assertTrue(violations.size() >= 5); // At least nameUser, userId, userRole, loanDescriptionType, loanStatus, equipmentStatus

        // Check specific violations
        assertTrue(hasViolationForPath(violations, "nameUser"));
        assertTrue(hasViolationForPath(violations, "userId"));
        assertTrue(hasViolationForPath(violations, "userRole"));
        assertTrue(hasViolationForPath(violations, "loanDescriptionType"));
        assertTrue(hasViolationForPath(violations, "loanStatus"));
        assertTrue(hasViolationForPath(violations, "equipmentStatus"));

        // Complete all required fields
        loan.setNameUser("Juan Perez");
        loan.setUserId("12345");
        loan.setUserRole("Estudiante");
        loan.setLoanDescriptionType("Description");
        loan.setLoanStatus("Prestado");
        loan.setEquipmentStatus("En buen estado");

        // Validate again
        violations = validator.validate(loan);

        // Check that there are no more violations
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Test validation for userRole pattern")
    void testValidationForUserRolePattern() {
        // Create a loan with invalid userRole
        LoanArticle loan = LoanArticle.builder()
                .articleIds(Arrays.asList(1, 2, 3))
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("InvalidRole")
                .loanDescriptionType("Description")
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .build();

        // Validate the loan
        Set<ConstraintViolation<LoanArticle>> violations = validator.validate(loan);

        // Check that there is a violation for userRole
        assertFalse(violations.isEmpty());
        boolean hasUserRoleViolation = hasViolationForPath(violations, "userRole");
        assertTrue(hasUserRoleViolation);

        // Test all valid roles
        String[] validRoles = {"Estudiante", "Docente", "Administrativo", "ServiciosGenerales"};
        for (String role : validRoles) {
            loan.setUserRole(role);
            violations = validator.validate(loan);
            hasUserRoleViolation = hasViolationForPath(violations, "userRole");
            assertFalse(hasUserRoleViolation, "Role '" + role + "' should be valid");
        }
    }

    @Test
    @DisplayName("Test validation for loanStatus pattern")
    void testValidationForLoanStatusPattern() {
        // Create a loan with invalid loanStatus
        LoanArticle loan = LoanArticle.builder()
                .articleIds(Arrays.asList(1, 2, 3))
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("Estudiante")
                .loanDescriptionType("Description")
                .loanStatus("InvalidStatus")
                .equipmentStatus("En buen estado")
                .build();

        // Validate the loan
        Set<ConstraintViolation<LoanArticle>> violations = validator.validate(loan);

        // Check that there is a violation for loanStatus
        assertFalse(violations.isEmpty());
        boolean hasLoanStatusViolation = hasViolationForPath(violations, "loanStatus");
        assertTrue(hasLoanStatusViolation);

        // Test all valid statuses
        String[] validStatuses = {"Prestado", "Vencido", "Devuelto"};
        for (String status : validStatuses) {
            loan.setLoanStatus(status);
            violations = validator.validate(loan);
            hasLoanStatusViolation = hasViolationForPath(violations, "loanStatus");
            assertFalse(hasLoanStatusViolation, "Status '" + status + "' should be valid");
        }
    }

    @Test
    @DisplayName("Test validation for equipmentStatus pattern")
    void testValidationForEquipmentStatusPattern() {
        // Create a loan with invalid equipmentStatus
        LoanArticle loan = LoanArticle.builder()
                .articleIds(Arrays.asList(1, 2, 3))
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("Estudiante")
                .loanDescriptionType("Description")
                .loanStatus("Prestado")
                .equipmentStatus("InvalidStatus")
                .build();

        // Validate the loan
        Set<ConstraintViolation<LoanArticle>> violations = validator.validate(loan);

        // Check that there is a violation for equipmentStatus
        assertFalse(violations.isEmpty());
        boolean hasEquipmentStatusViolation = hasViolationForPath(violations, "equipmentStatus");
        assertTrue(hasEquipmentStatusViolation);

        // Test all valid statuses
        String[] validStatuses = {"En buen estado", "Danado", "Requiere mantenimiento"};
        for (String status : validStatuses) {
            loan.setEquipmentStatus(status);
            violations = validator.validate(loan);
            hasEquipmentStatusViolation = hasViolationForPath(violations, "equipmentStatus");
            assertFalse(hasEquipmentStatusViolation, "Status '" + status + "' should be valid");
        }
    }

    @Test
    @DisplayName("Test size constraints for loanDescriptionType and devolutionRsegister")
    void testSizeConstraints() {
        // Create a string that exceeds the max size of 500
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            longString.append("0123456789");
        }
        String tooLongString = longString.toString();

        // Create a loan with a too long loanDescriptionType
        LoanArticle loan = LoanArticle.builder()
                .articleIds(Arrays.asList(1, 2, 3))
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("Estudiante")
                .loanDescriptionType(tooLongString)
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .build();

        // Validate the loan
        Set<ConstraintViolation<LoanArticle>> violations = validator.validate(loan);

        // Check that there is a violation for loanDescriptionType
        assertFalse(violations.isEmpty());
        boolean hasLoanDescriptionTypeViolation = hasViolationForPath(violations, "loanDescriptionType");
        assertTrue(hasLoanDescriptionTypeViolation);

        // Fix the violation
        loan.setLoanDescriptionType("Valid description");

        // Validate again
        violations = validator.validate(loan);

        // Check that there's no longer a violation for loanDescriptionType
        hasLoanDescriptionTypeViolation = hasViolationForPath(violations, "loanDescriptionType");
        assertFalse(hasLoanDescriptionTypeViolation);

        // Set a too long devolutionRsegister
        loan.setDevolutionRsegister(tooLongString);

        // Validate again
        violations = validator.validate(loan);

        // Check that there is a violation for devolutionRsegister
        assertFalse(violations.isEmpty());
        boolean hasDevolutionRsegisterViolation = hasViolationForPath(violations, "devolutionRsegister");
        assertTrue(hasDevolutionRsegisterViolation);

        // Fix the violation
        loan.setDevolutionRsegister("Valid devolution register");

        // Validate again
        violations = validator.validate(loan);

        // Check that there's no longer a violation for devolutionRsegister
        hasDevolutionRsegisterViolation = hasViolationForPath(violations, "devolutionRsegister");
        assertFalse(hasDevolutionRsegisterViolation);
    }

    @Test
    @DisplayName("Test equals and hashCode")
    void testEqualsAndHashCode() {
        // Create two equivalent loan articles
        LoanArticle loan1 = LoanArticle.builder()
                .id("1")
                .articleIds(Arrays.asList(1, 2, 3))
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("Estudiante")
                .loanDescriptionType("Description")
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .build();

        LoanArticle loan2 = LoanArticle.builder()
                .id("1")
                .articleIds(Arrays.asList(1, 2, 3))
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("Estudiante")
                .loanDescriptionType("Description")
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .build();

        // Test equals
        assertEquals(loan1, loan2);

        // Test hashCode
        assertEquals(loan1.hashCode(), loan2.hashCode());

        // Modify one loan
        loan2.setId("2");

        // Test they are no longer equal
        assertNotEquals(loan1, loan2);
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() {
        // Create a loan
        LoanArticle loan = LoanArticle.builder()
                .id("1")
                .articleIds(Arrays.asList(1, 2, 3))
                .nameUser("Juan Perez")
                .userId("12345")
                .userRole("Estudiante")
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .build();

        // Test toString returns a non-empty string
        String toString = loan.toString();
        assertNotNull(toString);
        assertFalse(toString.isEmpty());

        // Check that toString contains some expected values
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("nameUser=Juan Perez"));
        assertTrue(toString.contains("userId=12345"));
        assertTrue(toString.contains("userRole=Estudiante"));
    }

    @Test
    @DisplayName("Test all-args constructor")
    void testAllArgsConstructor() {
        // Create data for all fields
        String id = "1";
        List<Integer> articleIds = Arrays.asList(1, 2, 3);
        String nameUser = "Juan Perez";
        String userId = "12345";
        String userRole = "Estudiante";
        String loanDescriptionType = "Préstamo para proyecto";
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDate loanDate = LocalDate.now();
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(12, 0);
        LocalDate devolutionDate = loanDate.plusDays(7);
        String loanStatus = "Prestado";
        String equipmentStatus = "En buen estado";
        String devolutionRsegister = "Registro de devolución";

        // Create object with all-args constructor
        LoanArticle loan = new LoanArticle(
                id, articleIds, nameUser, userId, userRole, loanDescriptionType,
                creationDate, loanDate, startTime, endTime, devolutionDate,
                loanStatus, equipmentStatus, devolutionRsegister
        );

        // Verify all fields were set correctly
        assertEquals(id, loan.getId());
        assertEquals(articleIds, loan.getArticleIds());
        assertEquals(nameUser, loan.getNameUser());
        assertEquals(userId, loan.getUserId());
        assertEquals(userRole, loan.getUserRole());
        assertEquals(loanDescriptionType, loan.getLoanDescriptionType());
        assertEquals(creationDate, loan.getCreationDate());
        assertEquals(loanDate, loan.getLoanDate());
        assertEquals(startTime, loan.getStartTime());
        assertEquals(endTime, loan.getEndTime());
        assertEquals(devolutionDate, loan.getDevolutionDate());
        assertEquals(loanStatus, loan.getLoanStatus());
        assertEquals(equipmentStatus, loan.getEquipmentStatus());
        assertEquals(devolutionRsegister, loan.getDevolutionRsegister());
    }

    // Helper method to check if violations contain a specific path
    private boolean hasViolationForPath(Set<ConstraintViolation<LoanArticle>> violations, String path) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(path));
    }
}