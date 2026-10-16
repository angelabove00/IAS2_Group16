
package motorph_oop.service;

import motorph_oop.dao.AttendanceDAO;
import motorph_oop.security.AccessControl;
import motorph_oop.security.Permission;
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

    // Records employee check-in (own record only).
    public void checkIn(String empId,
                        String lastName,
                        String firstName) {

        AccessControl.requireSelf(empId);

        // Validate if employee is already checked in
        validateAlreadyCheckedIn(empId);

        // Record time-in
        attendanceDAO.recordTimeIn(empId, lastName, firstName);
    }

    // Records employee check-out (own record only).
    public void checkOut(String empId) {

        AccessControl.requireSelf(empId);

        attendanceDAO.recordTimeOut(empId);
    }

    // Retrieves attendance records: your own, or anyone's if you can view all employees.
    public List<Object[]> getAttendance(String empId) {

        AccessControl.requireSelfOr(empId, Permission.VIEW_ALL_EMPLOYEES);

        return attendanceDAO.getAttendanceByEmployee(empId);
    }

    // Retrieves attendance for one month with daily salary (own, or view-all roles).
    public List<Object[]> getAttendanceByEmployeeAndMonth(String empId,
                                                          String month,
                                                          double hourlyRate) {

        AccessControl.requireSelfOr(empId, Permission.VIEW_ALL_EMPLOYEES);

        return attendanceDAO.getAttendanceByEmployeeAndMonth(empId, month, hourlyRate);
    }

    // Validates whether the employee has already checked in.
    public void validateAlreadyCheckedIn(String empId) {

        // Optional enhancement later
    }

    // Retrieves available attendance months (own, or view-all roles).
    public List<String> getAvailableMonths(String empId) {

        AccessControl.requireSelfOr(empId, Permission.VIEW_ALL_EMPLOYEES);

        return attendanceDAO.getAvailableMonths(empId);
    }
}