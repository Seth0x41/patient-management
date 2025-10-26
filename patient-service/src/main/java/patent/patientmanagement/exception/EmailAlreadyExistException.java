package patent.patientmanagement.exception;

public class EmailAlreadyExistException extends RuntimeException {
    public EmailAlreadyExistException(String message) {

        super(message);
    }
}
