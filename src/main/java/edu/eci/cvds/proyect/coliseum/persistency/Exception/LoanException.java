package edu.eci.cvds.proyect.coliseum.persistency.Exception;


public class LoanException extends RuntimeException{

    public LoanException(String message) {
        super(message);
    }

    public static class LoanExceptionTimeError extends LoanException {

        public LoanExceptionTimeError(String message) {
            super(message);
        }
    }


    public static class LoanExceptionStateError extends LoanException {

        public LoanExceptionStateError(String message) {
            super(message);
        }
    }


    public static class LoanExceptionEstudianteHasPrestamo extends LoanException {

        public LoanExceptionEstudianteHasPrestamo(String message) {
            super(message);
        }
    }


    public static class LoanExceptionBookIsAvailable extends LoanException {

        public LoanExceptionBookIsAvailable(String message) {
            super(message);
        }
    }


    public static class LoanExceptionPrestamoIdNotFound extends LoanException {

        public LoanExceptionPrestamoIdNotFound(String message) { super(message);}
    }



    public static class LoanExceptionEstudianteHasNotPrestamo extends LoanException {

        public LoanExceptionEstudianteHasNotPrestamo(String message) {
            super(message);
        }
    }
}
