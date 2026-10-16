
package motorph_oop.service;

import motorph_oop.dao.LoginDAO;
import motorph_oop.model.*;
import motorph_oop.util.PasswordHasher;

public class LoginService {

    private LoginDAO loginDAO = new LoginDAO();

    // Residual risk R-003: shared temporary password, to be replaced by the Secure Password Reset control (Milestone 3).
    public static final String TEMP_PASSWORD = "1234";

    // login authentication
    public Employee authenticate(String username, String password) {

        String[] user = loginDAO.findUserByUsername(username);
        if (user == null) return null;

        String storedPassword = user[4];
        if (!PasswordHasher.verify(password, storedPassword)) return null;

        String empNo = user[0];
        String lastName = user[2];
        String firstName = user[3];
        String role = user[5];

        Employee employee;

        switch (role.toUpperCase()) {
            case "ADMIN":
                employee = new AdminAccess();
                break;
            case "IT":
                employee = new ITAccess();
                break;
            case "HR":
                employee = new HRAccess();
                break;
            case "FINANCE":
                employee = new FinanceAccess();
                break;
            default:
                employee = new EmployeeAccess();
        }

        employee.setEmpNo(empNo);
        employee.setLastName(lastName);
        employee.setFirstName(firstName);

        return employee;
    }

    // password validation rules
    public boolean validatePasswordRules(String password) {

        if (password.length() > 12) return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()].*");

        return hasUpper && hasNumber && hasSpecial;
    }

    // password strength check
    public String getPasswordStrength(String password) {

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()].*")) score++;

        if (score >= 4) return "Strong";
        else if (score >= 2) return "Normal";
        else return "Weak";
    }

    // reset password
    public boolean resetPassword(String username, String newPassword) {
        String hashed = PasswordHasher.hash(newPassword);
        return loginDAO.updatePassword(username, hashed);
    }

    // create user
    public boolean createUser(String empNo, String username, String lastName,
                              String firstName, String role) {

        return loginDAO.createUser(
                empNo,
                username,
                lastName,
                firstName,
                PasswordHasher.hash(TEMP_PASSWORD),
                role
        );
    }

    // get all users
    public java.util.List<String[]> getAllUsers() {
        return loginDAO.getAllUsers();
    }

    // delete user
    public boolean deleteUser(String username) {
        return loginDAO.deleteUser(username);
    }

    // check username existence
    public boolean usernameExists(String username) {
        return loginDAO.usernameExists(username);
    }

    // check if employee already has login
    public boolean employeeLoginExists(String empNo) {
        return loginDAO.employeeLoginExists(empNo);
    }

    // check if user is using default password
    public boolean isDefaultPassword(String username) {

        String[] user = loginDAO.findUserByUsername(username);
        if (user == null) return false;

        return PasswordHasher.verify(TEMP_PASSWORD, user[4]);
    }

    // change own password (for forced first-login flow)
    public boolean changeOwnPassword(String username, String currentPassword, String newPassword) {
        String[] user = loginDAO.findUserByUsername(username);
        if (user == null) return false;

        // Verify current password
        if (!PasswordHasher.verify(currentPassword, user[4])) {
            return false;
        }

        // Validate new password
        if (!validatePasswordRules(newPassword)) {
            return false;
        }

        // Reject TEMP_PASSWORD as new password
        if (newPassword.equals(TEMP_PASSWORD)) {
            return false;
        }

        // Hash and save new password
        String hashed = PasswordHasher.hash(newPassword);
        return loginDAO.updatePassword(username, hashed);
    }

    // password reset request list
    private static java.util.List<String> resetRequests =
            new java.util.ArrayList<>();

    // add reset request
    public void addResetRequest(String username) {
        if (!resetRequests.contains(username)) {
            resetRequests.add(username);
        }
    }

    // get all reset requests
    public java.util.List<String> getResetRequests() {
        return new java.util.ArrayList<>(resetRequests);
    }

    // remove request after reset
    public void removeResetRequest(String username) {
        resetRequests.remove(username);
    }

    // get user by username
    public String[] getUserByUsername(String username) {
        return loginDAO.findUserByUsername(username);
    }
}