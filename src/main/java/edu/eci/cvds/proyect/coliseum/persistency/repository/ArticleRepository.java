package edu.eci.cvds.proyect.coliseum.persistency.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


import edu.eci.cvds.proyect.coliseum.persistency.entity.Article;

@Repository
public interface ArticleRepository extends MongoRepository<Article, Integer> {
    Optional<List<Article>> findByArticleStatus(String articleStatus);
    Optional<List<Article>> findByName(String name);
    long countByNameAndArticleStatus(String name, String articleStatus);
    List<Article> findByArticleStatusAndIdNotIn(String articleStatus, Collection<Integer> ids);
}