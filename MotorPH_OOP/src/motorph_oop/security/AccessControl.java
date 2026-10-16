package motorph_oop.security;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import motorph_oop.util.Session;

// Single source of truth for "which role can do what".
public final class AccessControl {

    private static final Map<String, Set<Permission>> MATRIX = new HashMap<>();

    static {
        MATRIX.put("ADMIN", EnumSet.allOf(Permission.class));

        MATRIX.put("HR", EnumSet.of(
                Permission.VIEW_ALL_EMPLOYEES,
                Permission.ADD_EMPLOYEE,
                Permission.MODIFY_EMPLOYEE,
                Permission.DELETE_EMPLOYEE,
                Permission.APPROVE_LEAVE));

        MATRIX.put("FINANCE", EnumSet.of(
                Permission.VIEW_ALL_EMPLOYEES,
                Permission.CALCULATE_SALARY,
                Permission.GENERATE_PAYSLIP));

        MATRIX.put("IT", EnumSet.of(
                Permission.VIEW_ALL_EMPLOYEES,
                Permission.MANAGE_USER_ACCOUNTS));

        MATRIX.put("EMPLOYEE", EnumSet.noneOf(Permission.class));
    }

    private AccessControl() {}

    // Returns true/false. Use this in the UI to show or hide buttons.
    public static boolean can(Permission permission) {

        if (Session.role == null) return false;          // not logged in -> deny

        Set<Permission> allowed = MATRIX.get(Session.role.toUpperCase());

        return allowed != null && allowed.contains(permission);   // unknown role -> deny
    }

    // Throws if not allowed. Use this inside services.
    public static void require(Permission permission) {

        if (!can(permission)) {
            AuditLog.denied(permission.name());
            throw new AccessDeniedException("Access denied: " + permission);
        }
    }

    // Allows the user to access their own record; otherwise needs the permission.
    public static void requireSelfOr(String targetEmpNo, Permission permission) {

        if (Session.empNo != null && Session.empNo.equals(targetEmpNo)) return;

        require(permission);
    }

    // Only the logged-in user's own record is allowed (no role can override this).
    public static void requireSelf(String targetEmpNo) {

        if (Session.empNo != null && Session.empNo.equals(targetEmpNo)) return;

        AuditLog.denied("SELF_ONLY:" + targetEmpNo);
        throw new AccessDeniedException("Access denied: you can only act on your own record.");
    }
}