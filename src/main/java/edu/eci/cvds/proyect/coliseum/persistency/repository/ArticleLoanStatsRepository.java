package edu.eci.cvds.proyect.coliseum.persistency.repository;


import edu.eci.cvds.proyect.coliseum.persistency.entity.ArticleLoanStats;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleLoanStatsRepository extends MongoRepository<ArticleLoanStats, String> {
}