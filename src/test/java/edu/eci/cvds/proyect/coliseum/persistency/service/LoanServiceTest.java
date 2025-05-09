package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.repository.AlertRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.ArticleRepository;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private AlertRepository alertRepository;

    @InjectMocks private LoanService loanService;

    private Loan loan;
    private Article article;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        article = new Article();
        article.setId(1);
        article.setArticleStatus("Disponible");

        loan = new Loan();
        loan.setId("L001");
        loan.setUserId("user1");
        loan.setArticleIds(List.of(1));
        loan.setLoanStatus("Prestado");
        loan.setLoanDate(LocalDate.now());
    }

    @Test
    void shouldCreateLoanSuccessfully() {
        when(articleRepository.findAllById(anyList())).thenReturn(List.of(article));
        when(loanRepository.save(any())).thenReturn(loan);

        Loan result = loanService.createLoan(loan);

        assertEquals("Prestado", result.getLoanStatus());
        verify(articleRepository).saveAll(anyList());
        verify(loanRepository).save(any());
    }

    @Test
    void shouldThrowExceptionIfNoArticlesInLoan() {
        loan.setArticleIds(new ArrayList<>());

        LoanException exception = assertThrows(LoanException.class, () -> loanService.createLoan(loan));
        assertTrue(exception.getMessage().contains("al menos un artículo"));
    }

    @Test
    void shouldThrowExceptionIfArticleNotExists() {
        when(articleRepository.findAllById(anyList())).thenReturn(List.of());

        LoanException exception = assertThrows(LoanException.class, () -> loanService.createLoan(loan));
        assertTrue(exception.getMessage().contains("no existen"));
    }

    @Test
    void shouldThrowExceptionIfArticleIsUnavailable() {
        article.setArticleStatus("Prestado");
        when(articleRepository.findAllById(anyList())).thenReturn(List.of(article));

        LoanException exception = assertThrows(LoanException.class, () -> loanService.createLoan(loan));
        assertTrue(exception.getMessage().contains("no están disponibles"));
    }

    @Test
    void shouldReturnLoanOnDeleteIfNotReturnedOrExpired() {
        when(loanRepository.findById("L001")).thenReturn(Optional.of(loan));

        Loan deletedLoan = loanService.deleteLoanById("L001");

        assertEquals(loan, deletedLoan);
        verify(loanRepository).delete(loan);
        verify(articleRepository).saveAll(any());
    }

    @Test
    void shouldThrowExceptionOnDeleteIfReturned() {
        loan.setLoanStatus("Devuelto");
        when(loanRepository.findById("L001")).thenReturn(Optional.of(loan));

        assertThrows(LoanException.class, () -> loanService.deleteLoanById("L001"));
    }

    @Test
    void shouldThrowExceptionOnDeleteIfExpired() {
        loan.setLoanStatus("Vencido");
        when(loanRepository.findById("L001")).thenReturn(Optional.of(loan));

        assertThrows(LoanException.class, () -> loanService.deleteLoanById("L001"));
    }

    @Test
    void shouldUpdateLoanStatusAndArticles() {
        loan.setEquipmentStatus("Dañado");
        when(loanRepository.findById("L001")).thenReturn(Optional.of(loan));
        when(articleRepository.findById(1)).thenReturn(Optional.of(article));

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Devuelto");
        updates.put("equipmentStatus", "Dañado");

        loanService.updateLoan("L001", updates);

        verify(loanRepository, atLeastOnce()).save(any());
        verify(articleRepository, atLeastOnce()).saveAll(any());
    }

    @Test
    void shouldThrowExceptionIfLoanStatusInvalid() {
        loan.setLoanStatus("INVALIDO");
        when(articleRepository.findAllById(any())).thenReturn(List.of(article));

        assertThrows(LoanException.class, () -> loanService.createLoan(loan));
    }

    @Test
    void shouldMarkLoanAsExpiredAndSendAlert() {
        loan.setDevolutionDate(LocalDate.now().minusDays(1));
        when(loanRepository.findByLoanStatusAndDevolutionDateBefore(anyString(), any())).thenReturn(List.of(loan));

        loanService.verificarPrestamosVencidos();

        verify(alertRepository,atLeastOnce()).save(any(Alert.class));
        verify(loanRepository).save(any());
    }

    @Test
    void shouldSendReminderAlertForDueLoans() {
        loan.setDevolutionDate(LocalDate.now().plusDays(1));
        when(loanRepository.findByLoanStatusAndDevolutionDate(anyString(), any())).thenReturn(List.of(loan));

        loanService.enviarRecordatoriosDevolucion();

        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void shouldGetLoansByStatus() {
        when(loanRepository.findByLoanStatus("Prestado")).thenReturn(List.of(loan));

        List<Loan> result = loanService.getLoans("Prestado");

        assertEquals(1, result.size());
        assertEquals("Prestado", result.get(0).getLoanStatus());
    }

    @Test
    void shouldGetLoanByIdOrThrow() {
        when(loanRepository.findById("L001")).thenReturn(Optional.of(loan));
        Loan result = loanService.getLoanById("L001");
        assertEquals(loan, result);
    }

    @Test
    void shouldThrowWhenLoanIdNotFound() {
        when(loanRepository.findById("bad")).thenReturn(Optional.empty());
        assertThrows(LoanException.class, () -> loanService.getLoanById("bad"));
    }
}
