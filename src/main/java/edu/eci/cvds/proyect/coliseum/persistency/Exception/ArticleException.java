package edu.eci.cvds.proyect.coliseum.persistency.Exception;

public class ArticleException  extends RuntimeException{
    public ArticleException(String message) {
        super(message);
    }
    
    public static class ArticleExceptionArticleNotAvailable extends ArticleException {
        
        public ArticleExceptionArticleNotAvailable(String message) {
            super(message);
        }
    }
    public ArticleException(Integer articleId) {
        super("El artículo con ID " + articleId + " no está disponible.");
    }
}
