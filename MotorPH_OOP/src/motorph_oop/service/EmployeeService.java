
package motorph_oop.service;

import motorph_oop.dao.EmployeeDAO;
import motorph_oop.model.Employee;
import motorph_oop.model.RegularEmployee;
import motorph_oop.model.ProbationaryEmployee;

import java.util.List;

public class EmployeeService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    // get all employees
    public List<Employee> getEmployees() {
        return employeeDAO.getAllEmployees();
    }

    // generate next employee number
    public String generateNextEmployeeNumber() {
        int last = employeeDAO.getLastEmployeeNumber();
        return String.valueOf(last + 1);
    }

    // calculate gross rate
    public double calculateGrossRate(
            double basic,
            double rice,
            double phone,
            double clothing) {

        return basic + rice + phone + clothing;
    }

    // calculate hourly rate
    public double calculateHourlyRate(double basicSalary) {
        return basicSalary / 22 / 8;
    }

    // determine supervisor based on position
   
    public String determineSupervisor(String position) {

        if (position.equals("Chief Executive Officer"))
            return "N/A";

        if (position.equals("Chief Operating Officer") ||
            position.equals("Chief Finance Officer") ||
            position.equals("Chief Marketing Officer"))
            return "Garcia, Manuel III";

        if (position.equals("IT Operations and Systems") ||
            position.equals("HR Manager") ||
            position.equals("Account Manager"))
            return "Lim, Antonio";

        if (position.equals("HR Team Leader"))
            return "Villanueva, Andrea Mae";

        if (position.equals("HR Rank and File"))
            return "San, Jose Brad";

        if (position.equals("Accounting Head"))
            return "Aquino, Bianca Sofia";

        if (position.equals("Payroll Manager"))
            return "Alvaro, Roderick";

        if (position.equals("Payroll Team Leader") ||
            position.equals("Payroll Rank and File"))
            return "Salcedo, Anthony";

        if (position.equals("Account Team Leader"))
            return "Romualdez, Fredrick";

        if (position.equals("Account Rank and File"))
            return "Mata, Christian";

        if (position.equals("Sales & Marketing") ||
            position.equals("Supply Chain and Logistics") ||
            position.equals("Customer Service and Relations"))
            return "Reyes, Isabella";

        return "";
        }

    // validate name fields
    public void validateNames(String firstName, String lastName) {

        if (!firstName.matches("^[A-Za-z-]+$"))
            throw new IllegalArgumentException(
                    "First name cannot contain numbers or special characters.");

        if (!lastName.matches("^[A-Za-z-]+$"))
            throw new IllegalArgumentException(
                    "Last name cannot contain numbers or special characters.");
    }

    // convert string to number safely
    public double parseNumber(String value, String field) {

        try {

            double v = Double.parseDouble(value);

            if (v < 0)
                throw new IllegalArgumentException(field + " cannot be negative.");

            return v;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(field + " must be numeric.");
        }
    }

    // add new employee
    public void addEmployee(
            String empNo,
            String lastName,
            String firstName,
            String status,
            String position,
            String supervisor,
            String birthday,
            String address,
            String phone,
            String sss,
            String philhealth,
            String tin,
            String pagibig,
            String basicSalary,
            String riceSubsidy,
            String phoneAllowance,
            String clothingAllowance,
            String grossRate,
            String hourlyRate) {

        validateNames(firstName, lastName);

        Employee employee = determineEmployeeType(status);

        try {

            employee.setEmpNo(empNo);
            employee.setLastName(lastName);
            employee.setFirstName(firstName);
            employee.setBirthday(birthday);
            employee.setAddress(address);
            employee.setPhone(phone);
            employee.setSss(sss);
            employee.setPhilhealth(philhealth);
            employee.setTin(tin);
            employee.setPagibig(pagibig);
            employee.setStatus(status);
            employee.setPosition(position);
            employee.setSupervisor(supervisor);

            employee.setBasicSalary(Double.parseDouble(basicSalary));
            employee.setRiceSubsidy(Double.parseDouble(riceSubsidy));
            employee.setPhoneAllowance(Double.parseDouble(phoneAllowance));
            employee.setClothingAllowance(Double.parseDouble(clothingAllowance));
            employee.setGrossRate(Double.parseDouble(grossRate));
            employee.setHourlyRate(Double.parseDouble(hourlyRate));

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric input.");
        }

        validateEmployee(employee);
        employeeDAO.saveEmployee(employee);
    }

    // determine employee type
    private Employee determineEmployeeType(String status) {

        if (status.equalsIgnoreCase("Regular")) {
            return new RegularEmployee();
        } else {
            return new ProbationaryEmployee();
        }
    }

    // validate required fields
    private void validateEmployee(Employee employee) {

        if (employee.getLastName().isEmpty() ||
            employee.getFirstName().isEmpty() ||
            employee.getStatus().isEmpty() ||
            employee.getPosition().isEmpty()) {

            throw new IllegalArgumentException(
                    "Please fill in all required fields.");
        }
    }
}