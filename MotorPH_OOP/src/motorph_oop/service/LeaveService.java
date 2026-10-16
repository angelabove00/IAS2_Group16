
package motorph_oop.service;

import motorph_oop.dao.LeaveDAO;
import motorph_oop.model.LeaveRequest;
import motorph_oop.security.AccessControl;
import motorph_oop.security.AccessDeniedException;
import motorph_oop.security.AuditLog;
import motorph_oop.security.Permission;
import motorph_oop.util.Session;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LeaveService {

    private final LeaveDAO leaveDAO = new LeaveDAO();

    // true when the name belongs to the logged-in user
    private boolean isSelf(String employeeName) {

        if (Session.firstName == null || Session.lastName == null || employeeName == null) {
            return false;
        }

        String self = Session.firstName + " " + Session.lastName;
        return self.equalsIgnoreCase(employeeName.trim());
    }

    // all leave requests (Admin, HR)
    public List<LeaveRequest> getAllLeaves() {
        AccessControl.require(Permission.APPROVE_LEAVE);
        return leaveDAO.loadAllLeaves();
    }

    // your own leaves, or anyone's if you can approve leave
    public List<LeaveRequest> getLeavesByEmployee(String employeeName) {

        if (!isSelf(employeeName)) {
            AccessControl.require(Permission.APPROVE_LEAVE);
        }

        List<LeaveRequest> all = leaveDAO.loadAllLeaves();
        List<LeaveRequest> result = new ArrayList<>();

        for (LeaveRequest r : all) {

            if (r.getEmployeeName().equalsIgnoreCase(employeeName)) {
                result.add(r);
            }
        }

        return result;
    }

    // you can only submit leave for yourself
    public LeaveRequest submitLeave(
            String employeeName,
            String leaveType,
            LocalDate startDate,
            LocalDate endDate,
            String reason) {

        if (!isSelf(employeeName)) {
            AuditLog.denied("SUBMIT_LEAVE_FOR_OTHERS");
            throw new AccessDeniedException("Access denied: you can only submit leave for yourself.");
        }

        validateLeaveDates(startDate, endDate);

        long days = calculateLeaveDays(startDate, endDate);

        LeaveRequest request =
                new LeaveRequest(employeeName, leaveType, startDate, endDate, days, reason);

        leaveDAO.saveLeave(request);

        return request;
    }

    public void validateLeaveDates(LocalDate start, LocalDate end) {

        if (start == null || end == null)
            throw new IllegalArgumentException("Please select dates.");

        if (end.isBefore(start))
            throw new IllegalArgumentException("End date cannot be before start date.");
    }

    public long calculateLeaveDays(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    // approve leave (Admin, HR)
    public void approveLeave(LeaveRequest request) {
        AccessControl.require(Permission.APPROVE_LEAVE);
        changeLeaveStatus(request, "Approved");
    }

    // reject leave (Admin, HR)
    public void rejectLeave(LeaveRequest request) {
        AccessControl.require(Permission.APPROVE_LEAVE);
        changeLeaveStatus(request, "Rejected");
    }

    private void changeLeaveStatus(LeaveRequest request, String status) {

        request.setStatus(status);

        List<LeaveRequest> all = leaveDAO.loadAllLeaves();

        for (LeaveRequest r : all) {

            if (r.getEmployeeName().equals(request.getEmployeeName()) &&
                r.getStartDate().equals(request.getStartDate()) &&
                r.getEndDate().equals(request.getEndDate())) {

                r.setStatus(status);
            }
        }

        leaveDAO.overwriteAll(all);
    }
}