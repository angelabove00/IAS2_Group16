package motorph_oop.util;

public class Session {

    public static String role;
    public static String empNo;
    public static String firstName;
    public static String lastName;

    // Call this on logout so the next user never inherits the old role.
    public static void clear() {
        role = null;
        empNo = null;
        firstName = null;
        lastName = null;
    }
}