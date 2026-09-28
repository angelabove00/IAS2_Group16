
package motorph_oop.model;

// Defines role-based permissions within the system.
// Implementations determine access control capabilities.

public interface Role {

    // Determines if the role can manage employees.
    boolean canManageEmployees();

    // Determines if the role can approve leave requests.
    boolean canApproveLeave();

    //Determines if the role can calculate salary.
    boolean canCalculateSalary();

    // Determines if the role can view all employees.
    boolean canViewAllEmployees();

    //Determines if the role can modify employee details.
    boolean canModifyEmployeeDetails();

    // Determines if the role can generate payslips.
    boolean canGeneratePayslip();

    //Returns the name of the role.
    String getRoleName();
}