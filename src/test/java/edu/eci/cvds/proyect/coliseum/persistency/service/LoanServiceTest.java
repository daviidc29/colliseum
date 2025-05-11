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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static edu.eci.cvds.proyect.coliseum.persistency.service.LoanService.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private LoanService loanService;

    private Loan loan;
    private Article article1;
    private Article article2;

    @BeforeEach
    void setUp() {
        loan = Loan.builder()
                .id("loan123")
                .articleIds(Arrays.asList(1, 2))
                .nameUser("John Doe")
                .userId("user123")
                .userRole("Estudiante")
                .LoanDescriptionType("Préstamo de libros")
                .creationDate(LocalDateTime.now())
                .loanDate(LocalDate.now())
                .loanStatus(PRESTADO)
                .equipmentStatus("En buen estado")
                .devolutionRsegister("")
                .build();

        article1 = new Article(1, "Libro1", "Disponible", "Desc1", "/images/img1.png");
        article2 = new Article(2, "Libro2", "Disponible", "Desc2", "/images/img2.png");
    }

    @Test
    void testCreateLoan_Success() {
        when(articleRepository.findAllById(Arrays.asList(1, 2)))
                .thenReturn(Arrays.asList(article1, article2));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan result = loanService.createLoan(loan);

        assertNotNull(result);
        assertEquals("loan123", result.getId());
        // The method is called twice (1 for validation, 1 for status update).
        verify(articleRepository, times(2))
                .findAllById(Arrays.asList(1, 2));
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void testCreateLoan_NoArticlesException() {
        // Provide required non-null fields to avoid NullPointerException
        Loan invalidLoan = Loan.builder()
                .nameUser("Test User")
                .userId("testUserId")
                .userRole("Estudiante")
                .LoanDescriptionType("Descripción de prueba")
                .articleIds(new ArrayList<>()) // No articles, should trigger exception
                .loanStatus(PRESTADO)
                .equipmentStatus("En buen estado")
                .build();

        // Expect a LoanException because the list of articles is empty
        assertThrows(LoanException.class, () -> loanService.createLoan(invalidLoan));
    }

    @Test
    void testCreateLoan_NonExistingArticleException() {
        when(articleRepository.findAllById(Arrays.asList(1, 2)))
                .thenReturn(Collections.singletonList(article1)); // missing article2

        assertThrows(LoanException.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoan_ArticleAlreadyUnavailable() {
        article1.setArticleStatus("Prestado");
        when(articleRepository.findAllById(Arrays.asList(1, 2)))
                .thenReturn(Arrays.asList(article1, article2));

        assertThrows(LoanException.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoan_DevolutionDateInPast() {
        loan.setDevolutionDate(LocalDate.now().minusDays(1));
        when(articleRepository.findAllById(Arrays.asList(1, 2)))
                .thenReturn(Arrays.asList(article1, article2));
        assertThrows(LoanException.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoan_LoanDateAfterDevolutionDate() {
        loan.setLoanDate(LocalDate.now().plusDays(2));
        loan.setDevolutionDate(LocalDate.now().plusDays(1));
        when(articleRepository.findAllById(Arrays.asList(1, 2)))
                .thenReturn(Arrays.asList(article1, article2));
        assertThrows(LoanException.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testCreateLoan_InvalidLoanStatus() {
        loan.setLoanStatus("Invalido");
        when(articleRepository.findAllById(Arrays.asList(1, 2)))
                .thenReturn(Arrays.asList(article1, article2));
        assertThrows(LoanException.class, () -> loanService.createLoan(loan));
    }

    @Test
    void testDevolverLoan_Success() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        loanService.devolverLoan("loan123");

        verify(loanRepository).findById("loan123");
        verify(loanRepository).save(any(Loan.class));
        verify(articleRepository).findAllById(Arrays.asList(1, 2));
    }

    @Test
    void testDevolverLoan_LoanNotFound() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.empty());
        assertThrows(LoanException.class, () -> loanService.devolverLoan("loan123"));
    }

    @Test
    void testDeleteLoanById_Success() {
        loan.setLoanStatus(PRESTADO);
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));

        Loan deletedLoan = loanService.deleteLoanById("loan123");
        verify(loanRepository).delete(any(Loan.class));
        assertEquals("loan123", deletedLoan.getId());
    }

    @Test
    void testDeleteLoanById_DevueltoException() {
        loan.setLoanStatus(DEVUELTO);
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));

        assertThrows(LoanException.class, () -> loanService.deleteLoanById("loan123"));
    }

    @Test
    void testDeleteLoanById_VencidoException() {
        loan.setLoanStatus(VENCIDO);
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));

        assertThrows(LoanException.class, () -> loanService.deleteLoanById("loan123"));
    }

    @Test
    void testUpdateLoan_ChangeStatusToDevuelto() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", DEVUELTO);

        loanService.updateLoan("loan123", updates);

        verify(loanRepository, times(2)).save(any(Loan.class));
    }

    @Test
    void testUpdateLoan_ChangeStatusToVencido() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", VENCIDO);

        loanService.updateLoan("loan123", updates);

        // Remove the extra verification that expected a single call:
        // verify(loanRepository, times(1)).save(any(Loan.class));

        // Now verify that loanRepository.save(...) is called exactly twice in total
        // (once in updateLoan, once in markAsVencido).
        verify(loanRepository, times(2)).save(any());
    }

    @Test
    void testUpdateLoan_UpdateObservaciones() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));
        Map<String, Object> updates = new HashMap<>();
        updates.put("observaciones", "Cambio de descripcion");

        loanService.updateLoan("loan123", updates);
        assertEquals("Cambio de descripcion", loan.getLoanDescriptionType());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void testUpdateLoan_UpdateDevolutionDate() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));
        Map<String, Object> updates = new HashMap<>();
        updates.put("fecha_devolucion", LocalDate.now().plusDays(1));

        loanService.updateLoan("loan123", updates);

        verify(loanRepository).save(any(Loan.class));
        assertEquals(LocalDate.now().plusDays(1), loan.getDevolutionDate());
    }

    @Test
    void testMarkAsVencido_SetsStatusAndCreatesAlert() {
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        loanService.markAsVencido(loan);

        assertEquals(VENCIDO, loan.getLoanStatus());
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void testGetLoanById_NotFound() {
        when(loanRepository.findById("unknownLoanId")).thenReturn(Optional.empty());
        assertThrows(LoanException.class, () -> loanService.getLoanById("unknownLoanId"));
    }

    @Test
    void testGetLoanById_Found() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));
        Loan foundLoan = loanService.getLoanById("loan123");
        assertNotNull(foundLoan);
    }

    @Test
    void testGetLoansByUser_NotEmpty() {
        when(loanRepository.findByUserId("user123")).thenReturn(Collections.singletonList(loan));
        List<Loan> userLoans = loanService.getLoansByUser("user123");
        assertFalse(userLoans.isEmpty());
    }

    @Test
    void testGetLoansByUser_EmptyThrowsException() {
        when(loanRepository.findByUserId("user123")).thenReturn(Collections.emptyList());
        assertThrows(LoanException.class, () -> loanService.getLoansByUser("user123"));
    }

    @Test
    void testGetLoans_FilterByStatus() {
        Loan loan2 = new Loan();
        loan2.setLoanStatus(DEVUELTO);

        when(loanRepository.findByLoanStatus(PRESTADO)).thenReturn(Collections.singletonList(loan));
        when(loanRepository.findByLoanStatus(DEVUELTO)).thenReturn(Collections.singletonList(loan2));
        when(loanRepository.findAll()).thenReturn(Arrays.asList(loan, loan2));

        // Prestado
        List<Loan> prestamos = loanService.getLoans(PRESTADO);
        assertEquals(1, prestamos.size());
        // Devuelto
        prestamos = loanService.getLoans(DEVUELTO);
        assertEquals(1, prestamos.size());
        // All
        prestamos = loanService.getLoans(null);
        assertEquals(2, prestamos.size());
    }

    @Test
    void testGetAvailableArticlesInInterval_Success() {
        when(loanRepository.findOverlappingLoans(eq(PRESTADO), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        loanService.getAvailableArticlesInInterval(LocalDate.now(), LocalDate.now().plusDays(2));
        verify(articleRepository).findByArticleStatus("Disponible");
    }

    @Test
    void testGetAvailableArticlesInInterval_InvalidDates() {
        assertThrows(IllegalArgumentException.class,
                () -> loanService.getAvailableArticlesInInterval(null, LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> loanService.getAvailableArticlesInInterval(LocalDate.now().plusDays(2), LocalDate.now()));
    }

    @Test
    void testGetLoansByDateRangeAndStatus_Success() {
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(5);
        when(loanRepository.findByLoanDateBetweenAndLoanStatus(start, end, PRESTADO))
                .thenReturn(Collections.singletonList(loan));
        List<Loan> result = loanService.getLoansByDateRangeAndStatus(start, end, PRESTADO);
        assertEquals(1, result.size());
    }

    @Test
    void testGetLoansByDateRangeAndStatus_DatesInvalid() {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now();
        assertThrows(LoanException.class,
                () -> loanService.getLoansByDateRangeAndStatus(start, end, PRESTADO));
    }

    @Test
    void testGetLoansByUserReport() {
        when(loanRepository.findByUserId("user123")).thenReturn(Collections.singletonList(loan));
        List<Loan> result = loanService.getLoansByUserReport("user123");
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateArticlesStatus_Success() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));
        when(articleRepository.findById(1)).thenReturn(Optional.of(article1));
        when(articleRepository.findById(2)).thenReturn(Optional.of(article2));

        Map<String, String> updates = new HashMap<>();
        updates.put("1", "Dañado");
        updates.put("2", "Perdido");

        loanService.updateArticlesStatus("loan123", updates);

        verify(articleRepository, times(2)).save(any(Article.class));
    }

    @Test
    void testUpdateArticlesStatus_InvalidArticleId() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));
        Map<String, String> updates = new HashMap<>();
        updates.put("X", "Dañado");
        assertThrows(IllegalArgumentException.class, () -> loanService.updateArticlesStatus("loan123", updates));
    }

    @Test
    void testUpdateArticlesStatus_ArticleNotInLoan() {
        when(loanRepository.findById("loan123")).thenReturn(Optional.of(loan));
        Map<String, String> updates = new HashMap<>();
        updates.put("999", "Disponible");
        assertThrows(IllegalArgumentException.class, () -> loanService.updateArticlesStatus("loan123", updates));
    }

    @Test
    void testScheduleEnviarRecordatoriosDevolucion() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        // We test the find query for loans
        when(loanRepository.findByLoanStatusAndDevolutionDate(PRESTADO, tomorrow))
                .thenReturn(Collections.singletonList(loan));

        loanService.enviarRecordatoriosDevolucion();

        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void testScheduleVerificarPrestamosVencidos() {
        when(loanRepository.findByLoanStatusAndDevolutionDateBefore(eq(PRESTADO), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(loan));

        loanService.verificarPrestamosVencidos();

        // markAsVencido() also saves an alert, so we expect two saves total:
        // one from markAsVencido(), one from the loop in verificarPrestamosVencidos().
        verify(alertRepository, times(2)).save(any(Alert.class));
    }
}