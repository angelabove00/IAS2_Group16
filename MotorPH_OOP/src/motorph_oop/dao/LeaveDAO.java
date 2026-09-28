
package motorph_oop.dao;

import motorph_oop.model.LeaveRequest;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// data access object for leave requests
// handles reading and writing to the leave_requests.csv file
public class LeaveDAO {

    // csv file path
    private static final String FILE_PATH =
            "src/motorph_oop/resources/leave_requests.csv";

    // load all leave requests from csv
    public List<LeaveRequest> loadAllLeaves() {

        List<LeaveRequest> leaves = new ArrayList<>();
        File file = new File(FILE_PATH);

        // return empty list if file does not exist
        if (!file.exists()) return leaves;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",");

                // create leave request object
                LeaveRequest request = new LeaveRequest(
                        parts[0],                 // employee name
                        parts[1],                 // leave type
                        LocalDate.parse(parts[2]),
                        LocalDate.parse(parts[3]),
                        Long.parseLong(parts[4]),
                        parts[5]                  // reason
                );

                // set status
                request.setStatus(parts[6]);

                leaves.add(request);
            }

        } catch (Exception e) {
            throw new RuntimeException("error loading leave file", e);
        }

        return leaves;
    }

    // save a new leave request
    public void saveLeave(LeaveRequest request) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH, true))) {

            writer.println(
                    request.getEmployeeName() + "," +
                    request.getLeaveType() + "," +
                    request.getStartDate() + "," +
                    request.getEndDate() + "," +
                    request.getTotalDays() + "," +
                    request.getReason().replace(",", " ") + "," +
                    request.getStatus()
            );

        } catch (Exception e) {
            throw new RuntimeException("error saving leave", e);
        }
    }

    // overwrite csv file with updated leave list
    public void overwriteAll(List<LeaveRequest> leaves) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {

            for (LeaveRequest r : leaves) {

                writer.println(
                        r.getEmployeeName() + "," +
                        r.getLeaveType() + "," +
                        r.getStartDate() + "," +
                        r.getEndDate() + "," +
                        r.getTotalDays() + "," +
                        r.getReason().replace(",", " ") + "," +
                        r.getStatus()
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("error updating leave file", e);
        }
    }
}