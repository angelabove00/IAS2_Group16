
package motorph_oop.service;

import motorph_oop.dao.AttendanceDAO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

// Service layer for handling attendance-related operations.
// Acts as a bridge between the UI layer and AttendanceDAO.

public class AttendanceService {

    // DAO instance for attendance database operations
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    // Records employee check-in.
    public void checkIn(String empId,
                        String lastName,
                        String firstName) {

        // Validate if employee is already checked in
        validateAlreadyCheckedIn(empId);

        // Record time-in
        attendanceDAO.recordTimeIn(empId, lastName, firstName);
    }

    //  Records employee check-out.
    public void checkOut(String empId) {

        attendanceDAO.recordTimeOut(empId);
    }

    // Retrieves attendance records of a specific employee.     
    public List<Object[]> getAttendance(String empId) {

        return attendanceDAO.getAttendanceByEmployee(empId);
    }

    // Validates whether the employee has already checked in.   
    public void validateAlreadyCheckedIn(String empId) {

        // Optional enhancement later
    }

    //Retrieves available attendance months for an employee.  
    public List<String> getAvailableMonths(String empId) {

        return attendanceDAO.getAvailableMonths(empId);
    }
}