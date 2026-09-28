
package motorph_oop.model;

public class ProbationaryEmployee extends Employee {

    private static final double PROBATION_RATE = 0.80;

    public ProbationaryEmployee() {
        setStatus("Probationary");
    }

    @Override
    public double computeSalary(double hoursWorked) {

        double basePay = getHourlyRate() * hoursWorked;

        // 80% of base salary
        basePay = basePay * PROBATION_RATE;

        // Only rice + phone allowance
        double totalAllowance =
                getRiceSubsidy() +
                getPhoneAllowance();

        return basePay + totalAllowance;
    }

    @Override
    public String toString() {
        return "[Probationary] " + super.toString();
    }
}