
package motorph_oop.model;

public class ITAccess extends Employee implements Role {

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
        return false;
    }

    @Override
    public boolean canGeneratePayslip() {
        return false;
    }

    @Override
    public String getRoleName() {
        return "IT";
    }

    public String getDepartment() {
        return "IT Department";
    }
}