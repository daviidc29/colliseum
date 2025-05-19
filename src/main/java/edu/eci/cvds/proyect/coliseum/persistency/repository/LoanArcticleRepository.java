package edu.eci.cvds.proyect.coliseum.persistency.repository;

import edu.eci.cvds.proyect.coliseum.persistency.entity.LoanArticle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface LoanArcticleRepository extends MongoRepository<LoanArticle, String> {
    List<LoanArticle> findByLoanStatus(String loanStatus);

    List<LoanArticle> findByUserId(String userId);

    @Query("{ 'loanStatus' : ?0, $or: [ { $and: [ { 'loanDate' : { $lte: ?2 } }, { 'devolutionDate' : { $gte: ?1 } } ] }, { $and: [ { 'loanDate' : { $lte: ?2 } }, { 'devolutionDate' : null } ] } ] }")
    List<LoanArticle> findOverlappingLoans(String loanStatus, LocalDate startDate, LocalDate endDate);

    List<LoanArticle> findByLoanDateBetween(LocalDate startDate, LocalDate endDate);

    List<LoanArticle> findByLoanDateBetweenAndLoanStatus(LocalDate startDate, LocalDate endDate, String loanStatus);

    List<LoanArticle> findByLoanStatusAndDevolutionDate(String loanStatus, LocalDate devolutionDate);

    List<LoanArticle> findByLoanStatusAndDevolutionDateBefore(String loanStatus, LocalDate date);
}