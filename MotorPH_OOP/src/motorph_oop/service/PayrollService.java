package motorph_oop.utils;

import motorph_oop.util.Constants;
import motorph_oop.model.Employee;
import motorph_oop.security.AccessControl;
import motorph_oop.security.Permission;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/// Handles payroll computation including salary, deductions, and tax calculation.
 
public class PayrollService {

    // Contribution and deduction constants
    private static final double SSS_RATE_MIN = 135.00;
    private static final double SSS_RATE_MAX = 1125.00;
    private static final double PHILHEALTH_RATE = 0.04;
    private static final double PHILHEALTH_CAP = 4000.00;
    private static final double PAGIBIG_CONTRIBUTION = 100.00;

    // Progressive tax table:
    // { lowerBound, upperBound, taxRate, baseTax }
    private static final double[][] TAX_TABLE = {
        {20833, 33333, 0.20, 0.00},
        {33333, 66667, 0.25, 2083.33},
        {66667, 166667, 0.30, 7083.33},
        {166667, 666667, 0.32, 22083.33},
        {666667, Double.MAX_VALUE, 0.35, 102083.33}
    };

    // Date and time formatters
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern(Constants.TIME_FORMAT);

    // Holds payroll computation results.
    public static class PayrollResult {

        private final double grossSalary;
        private final double sssDeduction;
        private final double philhealthDeduction;
        private final double pagibigDeduction;
        private final double withholdingTax;
        private final double netSalary;

        public PayrollResult(double grossSalary,
                             double sssDeduction,
                             double philhealthDeduction,
                             double pagibigDeduction,
                             double withholdingTax,
                             double netSalary) {

            this.grossSalary = grossSalary;
            this.sssDeduction = sssDeduction;
            this.philhealthDeduction = philhealthDeduction;
            this.pagibigDeduction = pagibigDeduction;
            this.withholdingTax = withholdingTax;
            this.netSalary = netSalary;
        }

        public double getGrossSalary() {
            return grossSalary;
        }

        public double getSssDeduction() {
            return sssDeduction;
        }

        public double getPhilhealthDeduction() {
            return philhealthDeduction;
        }

        public double getPagibigDeduction() {
            return pagibigDeduction;
        }

        public double getWithholdingTax() {
            return withholdingTax;
        }

        public double getNetSalary() {
            return netSalary;
        }
    }

    // Calculates payroll details (Admin, Finance, or the employee's own record).
    public static PayrollResult calculatePayroll(Employee employee,
                                                 String attendanceFile,
                                                 String selectedMonth) {

        AccessControl.requireSelfOr(employee.getEmpNo(), Permission.CALCULATE_SALARY);
        return compute(employee, attendanceFile, selectedMonth);
    }

    // Same computation, used when generating a payslip PDF (Admin, Finance, or own record).
    public static PayrollResult generatePayslip(Employee employee,
                                                String attendanceFile,
                                                String selectedMonth) {

        AccessControl.requireSelfOr(employee.getEmpNo(), Permission.GENERATE_PAYSLIP);
        return compute(employee, attendanceFile, selectedMonth);
    }

    private static PayrollResult compute(Employee employee,
                                         String attendanceFile,
                                         String selectedMonth) {

        double hoursWorked =
                calculateTotalHoursWorked(
                        employee.getEmpNo(),
                        attendanceFile,
                        selectedMonth
                );

        // Polymorphism: salary computation depends on employee type
        double grossSalary = employee.computeSalary(hoursWorked);

        double sssDeduction =
                calculateSSSDeduction(employee.getBasicSalary());

        double philhealthDeduction =
                calculatePhilHealthDeduction(employee.getBasicSalary());

        double pagibigDeduction =
                calculatePagIBIGDeduction();

        double taxableIncome =
                grossSalary -
                (sssDeduction + philhealthDeduction + pagibigDeduction);

        double withholdingTax =
                calculateWithholdingTax(taxableIncome);

        double netSalary =
                grossSalary -
                (sssDeduction + philhealthDeduction +
                 pagibigDeduction + withholdingTax);

        return new PayrollResult(
                grossSalary,
                sssDeduction,
                philhealthDeduction,
                pagibigDeduction,
                withholdingTax,
                netSalary
        );
    }

    // Calculates total hours worked based on attendance records.
    private static double calculateTotalHoursWorked(String empNo,
                                                    String attendanceFile,
                                                    String selectedMonth) {

        double totalHours = 0.0;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(attendanceFile))) {

            String line;
            boolean skipHeader = true;

            while ((line = reader.readLine()) != null) {

                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (parts.length >= 6 &&
                        parts[0].trim().equals(empNo)) {

                    try {
                        LocalDate date =
                                LocalDate.parse(
                                        parts[3].trim(),
                                        DATE_FORMATTER
                                );

                        String monthYear =
                                date.getMonth()
                                        .getDisplayName(
                                                TextStyle.FULL,
                                                Locale.ENGLISH
                                        )
                                + " " + date.getYear();

                        if ("All".equals(selectedMonth)
                                || monthYear.equals(selectedMonth)) {

                            LocalTime logIn =
                                    LocalTime.parse(
                                            parts[4].trim(),
                                            TIME_FORMATTER
                                    );

                            LocalTime logOut =
                                    parts[5].trim().isEmpty()
                                            ? LocalTime.now()
                                            : LocalTime.parse(
                                                    parts[5].trim(),
                                                    TIME_FORMATTER
                                            );

                            totalHours +=
                                    Duration.between(logIn, logOut)
                                            .toMinutes() / 60.0;
                        }

                    } catch (Exception ignored) {
                    }
                }
            }

        } catch (IOException ignored) {
        }

        return totalHours;
    }

    // Calculates SSS deduction. 
    private static double calculateSSSDeduction(double basicSalary) {

        if (basicSalary < 4000) {
            return SSS_RATE_MIN;
        }

        if (basicSalary >= 29750) {
            return SSS_RATE_MAX;
        }

        return SSS_RATE_MIN;
    }

    // Calculates PhilHealth deduction.  
    private static double calculatePhilHealthDeduction(double basicSalary) {
        return Math.min(basicSalary * PHILHEALTH_RATE, PHILHEALTH_CAP);
    }

    // Returns Pag-IBIG deduction.   
    private static double calculatePagIBIGDeduction() {
        return PAGIBIG_CONTRIBUTION;
    }

    // Calculates withholding tax using progressive tax brackets.     
    private static double calculateWithholdingTax(double taxableIncome) {

        if (taxableIncome <= 20833) {
            return 0.0;
        }

        for (double[] bracket : TAX_TABLE) {

            double lowerBound = bracket[0];
            double upperBound = bracket[1];
            double rate = bracket[2];
            double baseTax = bracket[3];

            if (taxableIncome >= lowerBound &&
                taxableIncome < upperBound) {

                return ((taxableIncome - lowerBound) * rate) + baseTax;
            }
        }

        return 0.0;
    }
}