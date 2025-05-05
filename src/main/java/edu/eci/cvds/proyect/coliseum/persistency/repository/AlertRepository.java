package edu.eci.cvds.proyect.coliseum.persistency.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import edu.eci.cvds.proyect.coliseum.persistency.entity.Alert;
@Repository
public interface AlertRepository extends MongoRepository<Alert, String> {

}
