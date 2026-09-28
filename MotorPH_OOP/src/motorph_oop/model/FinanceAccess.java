
package motorph_oop.model;

public class FinanceAccess extends Employee implements Role {

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
        return true;
    }

    @Override
    public boolean canViewAllEmployees() {
        return true;
    }

    @Override
    public boolean canModifyEmployeeDetails() {
        return false;
    }

    @Override
    public boolean canGeneratePayslip() {
        return true;
    }

    @Override
    public String getRoleName() {
        return "Finance";
    }

    public String getDepartment() {
        return "Finance";
    }
}