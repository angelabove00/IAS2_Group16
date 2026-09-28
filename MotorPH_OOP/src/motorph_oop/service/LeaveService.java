
package motorph_oop.service;

import motorph_oop.dao.LeaveDAO;
import motorph_oop.model.LeaveRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LeaveService {

    private final LeaveDAO leaveDAO = new LeaveDAO();

    public List<LeaveRequest> getAllLeaves() {
        return leaveDAO.loadAllLeaves();
    }

    public List<LeaveRequest> getLeavesByEmployee(String employeeName) {

        List<LeaveRequest> all = leaveDAO.loadAllLeaves();
        List<LeaveRequest> result = new ArrayList<>();

        for (LeaveRequest r : all) {

            if (r.getEmployeeName().equalsIgnoreCase(employeeName)) {
                result.add(r);
            }
        }

        return result;
    }

    public LeaveRequest submitLeave(
            String employeeName,
            String leaveType,
            LocalDate startDate,
            LocalDate endDate,
            String reason) {

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

    public void approveLeave(LeaveRequest request) {
        changeLeaveStatus(request, "Approved");
    }

    public void rejectLeave(LeaveRequest request) {
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