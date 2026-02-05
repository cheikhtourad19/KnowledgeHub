package tn.esprim.knowledgehub.auth.exception;

public class ProfileCreationException extends RuntimeException {
    public ProfileCreationException(String message) {
        super(message);
    }

    public ProfileCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}