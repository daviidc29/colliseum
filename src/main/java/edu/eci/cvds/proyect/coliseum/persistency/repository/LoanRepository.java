package edu.eci.cvds.proyect.coliseum.persistency.repository;

import edu.eci.cvds.proyect.coliseum.persistency.entity.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface LoanRepository extends MongoRepository<Loan, String> {
    List<Loan> findByLoanStatus(String loanStatus);
    List<Loan> findByUserId(String userId);

    @Query("{ 'loanStatus' : ?0, $or: [ { $and: [ { 'loanDate' : { $lte: ?2 } }, { 'devolutionDate' : { $gte: ?1 } } ] }, { $and: [ { 'loanDate' : { $lte: ?2 } }, { 'devolutionDate' : null } ] } ] }")
    List<Loan> findOverlappingLoans(String loanStatus, LocalDate startDate, LocalDate endDate);

    List<Loan> findByLoanDateBetween(LocalDate startDate, LocalDate endDate);
    List<Loan> findByLoanDateBetweenAndLoanStatus(LocalDate startDate, LocalDate endDate, String loanStatus);

}
