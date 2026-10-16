package motorph_oop.security;

// Thrown when a logged-in user tries an action their role is not allowed to do.
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}