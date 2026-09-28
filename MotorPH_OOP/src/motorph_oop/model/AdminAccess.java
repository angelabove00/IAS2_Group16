
package motorph_oop.model;

public class AdminAccess extends Employee implements Role {

    @Override
    public double computeSalary(double hoursWorked) {
        return getHourlyRate() * hoursWorked;
    }

    @Override
    public boolean canManageEmployees() {
        return true;
    }

    @Override
    public boolean canApproveLeave() {
        return true;
    }

    @Override
    public boolean canCalculateSalary() {
        return true;
    }

    @Override
    public boolean canViewAllEmployees() {
        return true;
    }

    @Override
    public boolean canModifyEmployeeDetails() {
        return true;
    }

    @Override
    public boolean canGeneratePayslip() {
        return true;
    }

    @Override
    public String getRoleName() {
        return "Admin";
    }

    public String getDepartment() {
        return "Administration";
    }
}