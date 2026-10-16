
package motorph_oop.ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import motorph_oop.dao.EmployeeDAO;
import motorph_oop.model.Employee;
import motorph_oop.service.LoginService;

// panel for managing login accounts
public class LoginManagementPanel extends JFrame {

    private LoginService loginService = new LoginService();
    private EmployeeDAO employeeDAO = new EmployeeDAO();

    public LoginManagementPanel() {

        setTitle("Login Management");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        JButton btnCreateLogin = new JButton("Create User");
        JButton btnResetPassword = new JButton("Reset Password");
        JButton btnDeleteCredentials = new JButton("Delete User");
        JButton btnViewRequests = new JButton("Password Reset Requests");

        add(btnCreateLogin);
        add(btnResetPassword);
        add(btnDeleteCredentials);
        add(btnViewRequests);

        btnCreateLogin.addActionListener(e -> createUser());
        btnResetPassword.addActionListener(e -> resetPassword());
        btnDeleteCredentials.addActionListener(e -> deleteUser());
        btnViewRequests.addActionListener(e -> viewResetRequests());

        setVisible(true);
    }

    // create user login for employee
    private void createUser() {

        String empNo = JOptionPane.showInputDialog(this, "Enter Employee Number:");
        if (empNo == null || empNo.trim().isEmpty()) return;

        Employee emp = employeeDAO.findEmployeeById(empNo);

        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Employee not found.");
            return;
        }

        if (loginService.employeeLoginExists(empNo)) {
            JOptionPane.showMessageDialog(this,
                    "This employee already has a login account.");
            return;
        }

        String firstName = emp.getFirstName();
        String lastName = emp.getLastName();

        // generate username automatically
        String baseUsername =
                firstName.substring(0,1).toUpperCase() +
                lastName.toLowerCase();

        String username = baseUsername;
        int counter = 2;

        while (loginService.usernameExists(username)) {
            username = baseUsername + counter;
            counter++;
        }

        String[] roles = {"Admin","IT","HR","Finance","Employee"};

        String role = (String) JOptionPane.showInputDialog(
                this,
                "Select Access Level:",
                "Access Level",
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                roles[4]
        );

        if (role == null) return;

        boolean created = loginService.createUser(
                empNo,
                username,
                lastName,
                firstName,
                role
        );

        if (created) {
            JOptionPane.showMessageDialog(this,
                    "User created.\nUsername: " +
                    username +
                    "\nDefault Password: " + LoginService.TEMP_PASSWORD);
        }
    }

    // reset password for selected user
    private void resetPassword() {

        List<String[]> users = loginService.getAllUsers();

        String[] usernames = users.stream()
                .map(u -> u[1])
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select User:",
                "Reset Password",
                JOptionPane.PLAIN_MESSAGE,
                null,
                usernames,
                usernames[0]
        );

        if (selected == null) return;

        boolean success = loginService.resetPassword(selected, LoginService.TEMP_PASSWORD);

        JOptionPane.showMessageDialog(this,
                success ? "Password reset to the temporary default password." : "Reset failed");
    }

    // delete login credentials
    private void deleteUser() {

        List<String[]> users = loginService.getAllUsers();

        String[] usernames = users.stream()
                .map(u -> u[1])
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select User:",
                "Delete User",
                JOptionPane.PLAIN_MESSAGE,
                null,
                usernames,
                usernames[0]
        );

        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete " + selected + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean deleted = loginService.deleteUser(selected);

        JOptionPane.showMessageDialog(this,
                deleted ? "User deleted." : "Delete failed.");
    }

    // view password reset requests
    private void viewResetRequests() {

        List<String> requests = loginService.getResetRequests();

        if (requests.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No pending password reset requests."
            );
            return;
        }

        String[] users = requests.toArray(new String[0]);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Select user to reset password:",
                "Reset Requests",
                JOptionPane.PLAIN_MESSAGE,
                null,
                users,
                users[0]
        );

        if (selected == null) return;

        boolean success = loginService.resetPassword(selected, LoginService.TEMP_PASSWORD);

        if (success) {
            loginService.removeResetRequest(selected);

            JOptionPane.showMessageDialog(
                    this,
                    "Password reset to the temporary default password."
            );
        }
    }
}