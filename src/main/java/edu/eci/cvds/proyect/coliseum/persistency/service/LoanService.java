/*package edu.eci.cvds.proyect.coliseum.persistency.service;

import edu.eci.cvds.proyect.coliseum.persistency.Exception.LoanException;
import edu.eci.cvds.proyect.coliseum.persistency.dto.DevolutionDto;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import edu.eci.cvds.proyect.coliseum.persistency.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {
    public static final String VENCIDO="Vencido";
    public static final String DEVUELTO="Devuelto";
    private final LoanRepository loanRepository;

    @Autowired
    public LoanService(LoanRepository loanRepository) {
        this.loanRepository=loanRepository;

    }

    public Loan createLoan(Loan loan){
        loan.setLoanDate(LocalDate.now());
        loan.setCreationDate(LocalDateTime.now());
        loanValidations(loan);
        return loanRepository.save(loan);
    }

    private void loanValidations(Loan loan) {

        if (loan.getDevolutionDate() != null && loan.getLoanDate().isAfter(loan.getDevolutionDate())) {
            throw new LoanException.LoanExceptionTimeError("La fecha de préstamo no puede ser después de la fecha de devolución");
        }
        if (!loan.getLoanStatus().matches("Prestado|Vencido|Devuelto")) {
            throw new LoanException.LoanExceptionStateError("El estado solo puede ser Prestado, Vencido o Devuelto");
        }

    }
    public List<Loan> getLoans(String estado) {
        if (estado == null) {
            return getLoans();
        } else {
            switch (estado) {
                case "Prestado":
                    return getLoansPrestado();
                case "Vencido":
                    return getLoansVencido();
                case "Devuelto":
                    return getLoansDevuelto();
                default:
                    throw new LoanException.LoanExceptionStateError("El estado solo puede ser Prestado, Vencido o Devuelto");
            }
        }
    }


    private List<Loan> getLoans() {
        return loanRepository.findAll();
    }


    private List<Loan> getLoansPrestado() {
        return loanRepository.findByEstado("Prestado");
    }


    private List<Loan> getLoansVencido() {
        return loanRepository.findByEstado(VENCIDO);
    }


    private List<Loan> getLoansDevuelto() {
        return loanRepository.findByEstado(DEVUELTO);
    }



    public Loan getLoanById(String id) {
        return loanRepository.findById(id).orElseThrow(() ->
                new LoanException.LoanExceptionPrestamoIdNotFound("El préstamo con el id " + id + " no existe"));
    }




    public List<Loan> getLoansByIdEstudiante(String id) {
        List<Loan> loans =  loanRepository.findByUserId(id);
        if (loans.isEmpty()) {
            throw new LoanException.LoanExceptionEstudianteHasNotPrestamo("El estudiante con el id " + id + " no tiene préstamos o no existe.");
        }
        return loans;
    }


    public Loan deletePrestamoById(String id) {
        Loan loan = getLoanById(id);
        if (loan.getLoanStatus().equals(DEVUELTO)) {
            throw new LoanException.LoanExceptionStateError("El préstamo ya ha sido devuelto");
        } else if (loan.getLoanStatus().equals(VENCIDO)) {
            throw new LoanException.LoanExceptionStateError("El préstamo está vencido");
        } else {
            loanRepository.deleteById(loan.getId());
            return loan;
        }
    }


    public void updateLoan(String id, Map<String, Object> updates) {
        Loan loan= getLoanById(id);
        if ((VENCIDO.equals(loan.getLoanStatus()) || DEVUELTO.equals(loan.getLoanStatus())) && !updates.containsKey("historial_estado")) {
            throw new IllegalArgumentException("No se puede actualizar el préstamo en estado vencido o devuelto, excepto el historial del ejemplar");
        }
        updates.forEach((key, value) -> {
            switch (key) {
                case "observaciones":
                    loan.setLoanDescriptionType((String) value);
                    break;
                case "estado":
                    loan.setLoanStatus((String) value);
                    break;
                case "fecha_devolucion":
                    if (value instanceof String) {
                        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                        LocalDateTime fechaDevolucion = LocalDateTime.parse((String) value, formatter);
                        loan.setDevolutionDate(LocalDate.from(fechaDevolucion));
                    } else if (value instanceof LocalDateTime) {
                        loan.setDevolutionDate(LocalDate.from((LocalDateTime) value));
                    } else {
                        throw new IllegalArgumentException("Formato de fecha_devolucion no válido");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Atributo no válido: " + key);
            }
        });
        loanRepository.save(loan);
    }

    public Loan devolverLoan(String loanId, String loanStatus) {
        Loan loan = getLoanById(loanId);
        loan.setLoanStatus("Devuelto");
        loan.setDevolutionDate(LocalDate.now());
        loanRepository.save(loan);


        DevolutionDto devolutionDto = DevolutionDto.builder()
                .userId(loan.getUserId())
                .emailGuardian("")
                .build();
        return loan;
    }

    public void changeStatusOfLoanExpire(Loan loan){
        loan.setLoanStatus("Vencido");
        loanRepository.save(loan);
    }







}
*/