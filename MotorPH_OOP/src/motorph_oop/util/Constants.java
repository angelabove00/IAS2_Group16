
package motorph_oop.util;

import java.nio.file.Paths;

public class Constants {
    private static final String DATA_DIR = System.getProperty("motorph.dataDir");
    public static final String EMPLOYEE_DATA_CSV = DATA_DIR == null
            ? "src\\motorph_oop\\resources\\MotorPH_EmployeeData.csv"
            : Paths.get(DATA_DIR, "MotorPH_EmployeeData.csv").toString();
    public static final String ATTENDANCE_CSV = DATA_DIR == null
            ? "src\\motorph_oop\\resources\\MotorPH_AttendanceRecord.csv"
            : Paths.get(DATA_DIR, "MotorPH_AttendanceRecord.csv").toString();
    public static final String LOGIN_CSV = DATA_DIR == null
            ? "src\\motorph_oop\\resources\\MotorPH_EmployeeLogin.csv"
            : Paths.get(DATA_DIR, "MotorPH_EmployeeLogin.csv").toString();
    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final String TIME_FORMAT = "H:mm";
    public static final String[] EMPLOYEE_COLUMNS = {
        "EmployeeNo", "LastName", "FirstName", "Birthday", "Address", "Phone",
        "SSS", "Philhealth", "TIN", "Pagibig", "Status", "Position", "Supervisor",
        "BasicSalary", "RiceSubsidy", "PhoneAllowance", "ClothingAllowance",
        "GrossRate", "HourlyRate"
    };
    public static final String[] ATTENDANCE_COLUMNS = {"Date", "Log In", "Log Out", "Hours Worked", "Daily Salary"};
}