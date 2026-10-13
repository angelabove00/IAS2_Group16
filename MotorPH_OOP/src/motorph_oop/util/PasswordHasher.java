package motorph_oop.util;

import org.mindrot.jbcrypt.BCrypt;

// Control 1: one-way bcrypt hashing for login passwords.
public final class PasswordHasher {
    public static final int COST = 12;
    private PasswordHasher() {}

    public static String hash(String plain) {
        if (plain == null || plain.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(COST));
    }

    public static boolean verify(String plain, String storedHash) {
        if (plain == null || storedHash == null) {
            return false;
        }
        if (!isBcryptHash(storedHash)) {
            return false;
        }
        try {
            return BCrypt.checkpw(plain, storedHash);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isBcryptHash(String value) {
        if (value == null) {
            return false;
        }
        return value.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }
}
