package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    @Mock
    LoanRepository loanRepository;
    @Mock
    ArticleRepository articleRepository;
    @Mock
    AlertRepository alertRepository;

    @InjectMocks
    LoanService loanService;

    Loan sampleLoan;
    List<Integer> articleIds;
    Article article;
    LoanService.LoanStatus loanStatus;
    LoanService.ArticleStatus articleStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        articleIds = List.of(1, 2);
        article = new Article();
        article.setId(1);
        article.setArticleStatus(LoanService.ArticleStatus.DISPONIBLE.getValue());

        sampleLoan = Loan.builder()
                .id("loan1")
                .articleIds(articleIds)
                .nameUser("Juan")
                .userId("user1")
                .userRole("Estudiante")
                .loanDescriptionType("Libro")
                .creationDate(LocalDateTime.now())
                .loanDate(LocalDate.now())
                .devolutionDate(LocalDate.now().plusDays(2))
                .loanStatus(LoanService.LoanStatus.PRESTADO.getValue())
                .equipmentStatus("En buen estado")
                .build();
    }

    @Test
    void testCreateLoanSuccess() {
        when(articleRepository.findAllById(articleIds)).thenReturn(
                Arrays.asList(
                        Article.builder().id(1).articleStatus("Disponible").build(),
                        Article.builder().id(2).articleStatus("Disponible").build()
                )
        );
        when(loanRepository.save(any())).thenReturn(sampleLoan);

        Loan saved = loanService.createLoan(sampleLoan);

        assertNotNull(saved);
        verify(articleRepository).saveAll(anyList());
        verify(loanRepository).save(any());
    }

    @Test
    void testCreateLoanWithUnavailableArticleShouldThrow() {
        List<Article> articles = Arrays.asList(
                Article.builder().id(1).articleStatus("Disponible").build(),
                Article.builder().id(2).articleStatus("Prestado").build()
        );
        when(articleRepository.findAllById(articleIds)).thenReturn(articles);

        Loan loan = Loan.builder().articleIds(articleIds).loanStatus("Prestado")
                .nameUser("Juan").userId("user1").userRole("Estudiante")
                .loanDescriptionType("Libro").equipmentStatus("En buen estado")
                .build();

        LoanException.LoanExceptionBookIsAvailable ex = assertThrows(
                LoanException.LoanExceptionBookIsAvailable.class,
                () -> loanService.createLoan(loan)
        );
        assertTrue(ex.getMessage().contains("no están disponibles"));
    }

    @Test
    void testCreateLoanWithMissingArticleShouldThrow() {
        when(articleRepository.findAllById(articleIds)).thenReturn(List.of(article));

        Loan loan = Loan.builder().articleIds(articleIds).loanStatus("Prestado")
                .nameUser("Juan").userId("user1").userRole("Estudiante")
                .loanDescriptionType("Libro").equipmentStatus("En buen estado")
                .build();

        LoanException.LoanExceptionStateError ex = assertThrows(
                LoanException.LoanExceptionStateError.class,
                () -> loanService.createLoan(loan)
        );
        assertTrue(ex.getMessage().contains("no existen"));
    }

    @Test
    void testCreateLoanWithInvalidDatesShouldThrow() {
        Loan loan = Loan.builder()
                .articleIds(articleIds)
                .loanStatus("Prestado")
                .nameUser("Juan").userId("user1").userRole("Estudiante")
                .loanDescriptionType("Libro").equipmentStatus("En buen estado")
                .loanDate(LocalDate.of(2025, 5, 10))
                .devolutionDate(LocalDate.of(2025, 5, 1))
                .build();

        when(articleRepository.findAllById(articleIds)).thenReturn(
                Arrays.asList(
                        Article.builder().id(1).articleStatus("Disponible").build(),
                        Article.builder().id(2).articleStatus("Disponible").build()
                )
        );

        LoanException.LoanExceptionTimeError ex = assertThrows(
                LoanException.LoanExceptionTimeError.class,
                () -> loanService.createLoan(loan)
        );
        assertTrue(ex.getMessage().contains("no puede ser posterior"));
    }

    @Test
    void testCreateLoanWithPastDevolutionDateShouldThrow() {
        Loan loan = Loan.builder()
                .articleIds(articleIds)
                .loanStatus("Prestado")
                .nameUser("Juan").userId("user1").userRole("Estudiante")
                .loanDescriptionType("Libro").equipmentStatus("En buen estado")
                .loanDate(LocalDate.now())
                .devolutionDate(LocalDate.now().minusDays(1))
                .build();

        when(articleRepository.findAllById(articleIds)).thenReturn(
                Arrays.asList(
                        Article.builder().id(1).articleStatus("Disponible").build(),
                        Article.builder().id(2).articleStatus("Disponible").build()
                )
        );

        LoanException.LoanExceptionTimeError ex = assertThrows(
                LoanException.LoanExceptionTimeError.class,
                () -> loanService.createLoan(loan)
        );
        assertEquals("La fecha de préstamo no puede ser posterior a la de devolución", ex.getMessage());
    }

    @Test
    void testCreateLoanWithInvalidStatusShouldThrow() {
        Loan loan = Loan.builder()
                .articleIds(articleIds)
                .loanStatus("Inexistente")
                .nameUser("Juan").userId("user1").userRole("Estudiante")
                .loanDescriptionType("Libro").equipmentStatus("En buen estado")
                .build();

        when(articleRepository.findAllById(articleIds)).thenReturn(
                Arrays.asList(
                        Article.builder().id(1).articleStatus("Disponible").build(),
                        Article.builder().id(2).articleStatus("Disponible").build()
                )
        );

        LoanException.LoanExceptionStateError ex = assertThrows(
                LoanException.LoanExceptionStateError.class,
                () -> loanService.createLoan(loan)
        );
        assertTrue(ex.getMessage().contains("inválido"));
    }

    @Test
    void testDevolverLoanSuccess() {
        Loan loan = spy(sampleLoan);
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(articleIds)).thenReturn(Arrays.asList(article));
        when(loanRepository.save(any())).thenReturn(loan);

        loanService.devolverLoan("loan1");
        verify(loanRepository).save(any()); // Solo se guarda una vez
    }

    @Test
    void testDevolverLoanWithNullIdShouldThrow() {
        assertThrows(NullPointerException.class, () -> loanService.devolverLoan(null));
    }

    @Test
    void testDeleteLoanByIdPrestado() {
        Loan loan = spy(sampleLoan);
        loan.setLoanStatus(LoanService.LoanStatus.PRESTADO.getValue());
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(articleIds)).thenReturn(Arrays.asList(article));

        Loan deleted = loanService.deleteLoanById("loan1");
        verify(loanRepository).delete(any());
        assertEquals(sampleLoan.getId(), deleted.getId());
    }

    @Test
    void testDeleteLoanByIdDevueltoShouldThrow() {
        Loan loan = spy(sampleLoan);
        loan.setLoanStatus(LoanService.LoanStatus.DEVUELTO.getValue());
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));

        LoanException.LoanExceptionStateError ex = assertThrows(
                LoanException.LoanExceptionStateError.class,
                () -> loanService.deleteLoanById("loan1")
        );
        assertTrue(ex.getMessage().contains("devuelto"));
    }

    @Test
    void testUpdateLoanStatusDevuelto() {
        Loan loan = spy(sampleLoan);
        loan.setLoanStatus(LoanService.LoanStatus.PRESTADO.getValue());
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        when(articleRepository.findAllById(anyList())).thenReturn(
                Arrays.asList(Article.builder().id(1).articleStatus("Prestado").build())
        );
        doReturn(loan).when(loanRepository).save(any());

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", LoanService.LoanStatus.DEVUELTO.getValue());

        loanService.updateLoan("loan1", updates);

        verify(loanRepository, atLeastOnce()).save(any());
    }

    @Test
    void testUpdateLoanWithInvalidFieldShouldThrow() {
        Loan loan = spy(sampleLoan);
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));

        Map<String, Object> updates = new HashMap<>();
        updates.put("invalido", "valor");

        assertThrows(IllegalArgumentException.class, () -> loanService.updateLoan("loan1", updates));
    }

    @Test
    void testUpdateArticlesStatusWithInvalidArticleIdShouldThrow() {
        Loan loan = spy(sampleLoan);
        loan.setArticleIds(List.of(1));
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));

        Map<String, String> updates = new HashMap<>();
        updates.put("99", "Disponible");

        assertThrows(IllegalArgumentException.class, () -> loanService.updateArticlesStatus("loan1", updates));
    }

    @Test
    void testUpdateArticlesStatusWithInvalidStatusShouldThrow() {
        Loan loan = spy(sampleLoan);
        loan.setArticleIds(List.of(1));
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));

        Map<String, String> updates = new HashMap<>();
        updates.put("1", "Inexistente");

        assertThrows(IllegalArgumentException.class, () -> loanService.updateArticlesStatus("loan1", updates));
    }

    @Test
    void testGetLoanByIdSuccess() {
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(sampleLoan));
        Loan l = loanService.getLoanById("loan1");
        assertEquals("loan1", l.getId());
    }

    @Test
    void testGetLoanByIdNotFound() {
        when(loanRepository.findById("loanX")).thenReturn(Optional.empty());
        assertThrows(LoanException.LoanExceptionPrestamoIdNotFound.class, () -> loanService.getLoanById("loanX"));
    }

    @Test
    void testGetLoansByUserWithNoLoansShouldThrow() {
        when(loanRepository.findByUserId("userX")).thenReturn(Collections.emptyList());
        assertThrows(LoanException.LoanExceptionEstudianteHasNotPrestamo.class, () -> loanService.getLoansByUser("userX"));
    }

    @Test
    void testGetLoansByUserSuccess() {
        when(loanRepository.findByUserId("user1")).thenReturn(List.of(sampleLoan));
        List<Loan> loans = loanService.getLoansByUser("user1");
        assertEquals(1, loans.size());
    }

    @Test
    void testGetLoansByDateRangeAndStatus() {
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);
        when(loanRepository.findByLoanDateBetweenAndLoanStatus(from, to, "Prestado")).thenReturn(List.of(sampleLoan));

        List<Loan> loans = loanService.getLoansByDateRangeAndStatus(from, to, "Prestado");
        assertEquals(1, loans.size());
    }

    @Test
    void testGetLoansByDateRangeAndStatusInvalidStatusShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> loanService.getLoansByDateRangeAndStatus(LocalDate.now(), LocalDate.now().plusDays(1), "Invalid"));
    }

    @Test
    void testGetAvailableArticlesInInterval() {

    }

    @Test
    void testGetAvailableArticlesInIntervalWithUnavailableArticles() {
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(10);

        Loan otherLoan = Loan.builder()
                .articleIds(List.of(1, 2))
                .nameUser("Test User")
                .userId("user1")
                .userRole("Estudiante")
                .loanDescriptionType("Libro")
                .loanStatus("Prestado")
                .equipmentStatus("En buen estado")
                .build();

        when(loanRepository.findOverlappingLoans(anyString(), any(), any())).thenReturn(List.of(otherLoan));
        when(articleRepository.findByArticleStatusAndIdNotIn(anyString(), anySet())).thenReturn(List.of(article));

        Object result = loanService.getAvailableArticlesInInterval(from, to);
        assertNotNull(result);
        verify(articleRepository).findByArticleStatusAndIdNotIn(eq("Disponible"), anySet());
    }

    @Test
    void testValidateDateRangeStartAfterEndShouldThrow() {
        LocalDate from = LocalDate.now().plusDays(1);
        LocalDate to = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () -> loanService.getLoansByDateRangeAndStatus(from, to, null));
    }

    @Test
    void testEnviarRecordatoriosDevolucion() {
        Loan loan = spy(sampleLoan);
        loan.setDevolutionDate(LocalDate.now().plusDays(1));
        when(loanRepository.findByLoanStatusAndDevolutionDate(any(), any())).thenReturn(List.of(loan));
        loanService.enviarRecordatoriosDevolucion();
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void testVerificarPrestamosVencidos() {
        Loan loan = spy(sampleLoan);
        when(loanRepository.findByLoanStatusAndDevolutionDateBefore(any(), any())).thenReturn(List.of(loan));
        when(alertRepository.save(any(Alert.class))).thenReturn(null); // o puedes retornar un Alert simulado si prefieres
        when(articleRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        when(loanRepository.save(any())).thenReturn(loan);

        loanService.verificarPrestamosVencidos();

        verify(loanRepository, atLeastOnce()).save(any());
        verify(alertRepository, atLeastOnce()).save(any(Alert.class));
    }

    @Test
    void testMarkAsVencido() {
        Loan loan = spy(sampleLoan);
        loan.setLoanStatus(LoanService.LoanStatus.PRESTADO.getValue());
        when(articleRepository.findAllById(anyList())).thenReturn(List.of(article));
        loanService.markAsVencido(loan);
        verify(alertRepository).save(any(Alert.class));
        verify(loanRepository).save(any());
    }
}