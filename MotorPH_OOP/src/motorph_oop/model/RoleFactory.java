
package motorph_oop.model;

public class RoleFactory {

    public static Employee createRole(String role) {

        switch(role.toUpperCase()) {

            case "ADMIN":
                return new AdminAccess();

            case "IT":
                return new ITAccess();

            case "HR":
                return new HRAccess();

            case "FINANCE":
                return new FinanceAccess();

            default:
                return new EmployeeAccess();
        }
    }
}
