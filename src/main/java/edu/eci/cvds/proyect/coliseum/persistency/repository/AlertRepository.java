package edu.eci.cvds.proyect.coliseum.persistency.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;

import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {
    List<Alert> findByUserIdOrderByTimestampDesc(String userId);
    List<Alert> findByMessageContainingIgnoreCase(String keyword);

}
