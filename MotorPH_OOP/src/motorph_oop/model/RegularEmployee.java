
package motorph_oop.model;

public class RegularEmployee extends Employee {

    // Constructor
    public RegularEmployee() {
        setStatus("Regular");
    }

    // Override abstract method
    @Override
    public double computeSalary(double hoursWorked) {

        double basePay = getHourlyRate() * hoursWorked;

        // Regular employees get full allowances
        double totalAllowance =
                getRiceSubsidy() +
                getPhoneAllowance() +
                getClothingAllowance();

        return basePay + totalAllowance;
    }

    // Optional: Override toString for more detail
    @Override
    public String toString() {
        return "[Regular] " + super.toString();
    }
}