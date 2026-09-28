
package motorph_oop.dao;

import motorph_oop.util.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class AttendanceDAO {

    // system date and time format
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern(Constants.TIME_FORMAT);

    // convert date to "Month Year"
    private static String getMonthYear(LocalDate date) {
        return date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + date.getYear();
    }

    // read csv file and return rows
    private List<String[]> readAttendanceFile() {
        List<String[]> rows = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(Constants.ATTENDANCE_CSV), "UTF-8")) {

            if (scanner.hasNextLine()) scanner.nextLine(); // skip header

            while (scanner.hasNextLine()) {
                rows.add(scanner.nextLine().split(",", -1));
            }

        } catch (Exception e) {
            throw new RuntimeException("attendance load error", e);
        }

        return rows;
    }

    // get attendance by employee and selected month
    public List<Object[]> getAttendanceByEmployeeAndMonth(
            String empNo, String selectedMonth, double hourlyRate) {

        List<Object[]> result = new ArrayList<>();

        for (String[] parts : readAttendanceFile()) {

            if (parts.length < 6 || !parts[0].trim().equals(empNo))
                continue;

            LocalDate date = LocalDate.parse(parts[3].trim(), DATE_FORMAT);
            String monthYear = getMonthYear(date);

            if (!"All".equals(selectedMonth) && !monthYear.equals(selectedMonth))
                continue;

            LocalTime in = LocalTime.parse(parts[4].trim(), TIME_FORMAT);

            LocalTime out = parts[5].trim().isEmpty()
                    ? LocalTime.now()
                    : LocalTime.parse(parts[5].trim(), TIME_FORMAT);

            double hours = Duration.between(in, out).toMinutes() / 60.0;
            double dailySalary = hours * hourlyRate;

            result.add(new Object[]{
                    parts[3],
                    parts[4],
                    parts[5],
                    hours,
                    dailySalary
            });
        }

        return result;
    }

    // get months that contain attendance
    public List<String> getAvailableMonths(String empNo) {

        Set<String> months = new LinkedHashSet<>();

        for (String[] parts : readAttendanceFile()) {

            if (parts.length < 6 || !parts[0].trim().equals(empNo))
                continue;

            LocalDate date = LocalDate.parse(parts[3].trim(), DATE_FORMAT);
            months.add(getMonthYear(date));
        }

        return new ArrayList<>(months);
    }

    // record time-in
    public void recordTimeIn(String empId, String lastName, String firstName) {

        String today = LocalDate.now().format(DATE_FORMAT);
        String timeIn = LocalTime.now().format(TIME_FORMAT);

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(Constants.ATTENDANCE_CSV, true))) {

            writer.write(String.join(",",
                    empId,
                    lastName,
                    firstName,
                    today,
                    timeIn,
                    ""
            ));

            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException("time-in failed", e);
        }
    }

    // record time-out
    public void recordTimeOut(String empId) {

        String today = LocalDate.now().format(DATE_FORMAT);
        String timeOut = LocalTime.now().format(TIME_FORMAT);

        File original = new File(Constants.ATTENDANCE_CSV);
        File temp = new File("src/temp.csv");

        try (BufferedReader reader = new BufferedReader(new FileReader(original));
             BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {

            String line;
            boolean updated = false;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",", -1);

                if (!updated &&
                        parts.length >= 6 &&
                        parts[0].equals(empId) &&
                        parts[3].equals(today) &&
                        !parts[4].isEmpty() &&
                        parts[5].isEmpty()) {

                    parts[5] = timeOut;
                    line = String.join(",", parts);
                    updated = true;
                }

                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException("time-out failed", e);
        }

        try {
            Files.move(temp.toPath(), original.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("file replace failed", e);
        }
    }

    // get all attendance records for employee
    public List<Object[]> getAttendanceByEmployee(String empId) {

        List<Object[]> rows = new ArrayList<>();

        for (String[] parts : readAttendanceFile()) {

            if (parts.length >= 6 && parts[0].equals(empId)) {
                rows.add(new Object[]{
                        parts[3],
                        parts[4],
                        parts[5]
                });
            }
        }

        return rows;
    }
}