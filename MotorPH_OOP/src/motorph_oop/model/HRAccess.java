
package motorph_oop.model;
public class HRAccess extends Employee implements Role {

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
        return true;
    }

    @Override
    public boolean canModifyEmployeeDetails() {
        return true;
    }

    @Override
    public boolean canGeneratePayslip() {
        return false;
    }

    @Override
    public String getRoleName() {
        return "HR";
    }

    public String getDepartment() {
        return "Human Resources";
    }
}