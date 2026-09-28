
package motorph_oop.model;

// Logistics / Customer Service / Sales department

public class EmployeeAccess extends Employee implements Role {

    @Override
    public double computeSalary(double hoursWorked) {
        return getHourlyRate() * hoursWorked;
    }

    @Override
    public boolean canManageEmployees() {
        return false;
    }

    @Override
    public boolean canApproveLeave() {
        return false;
    }

    @Override
    public boolean canCalculateSalary() {
        return false;
    }

    @Override
    public boolean canViewAllEmployees() {
        return false;
    }

    @Override
    public boolean canModifyEmployeeDetails() {
        return false;
    }

    @Override
    public boolean canGeneratePayslip() {
        return false;
    }

    @Override
    public String getRoleName() {
        return "Employee";
    }

    public String getDepartment() {
        return "Customer Service / Logistics";
    }
}