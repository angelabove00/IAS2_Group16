package motorph_oop;

import motorph_oop.model.Employee;
import motorph_oop.security.AccessControl;
import motorph_oop.security.AccessDeniedException;
import motorph_oop.security.Permission;
import motorph_oop.service.AttendanceService;
import motorph_oop.service.EmployeeService;
import motorph_oop.service.LeaveService;
import motorph_oop.service.LoginService;
import motorph_oop.util.Constants;
import motorph_oop.util.Session;
import motorph_oop.utils.PayrollService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

// Control 2: Backend Role-Based Access Control (RBAC) tests.
// Run this file the same way as PasswordHashingTest (Run File / Shift+F6).
public class RBACTest {

    private static int passed = 0;
    private static int failed = 0;
    private static String testDataDir;

    // permission matrix approved for the system (written out separately on purpose,
    // so the test does not simply copy the code it is checking)
    private static final String[] ROLES = {"Admin", "HR", "Finance", "IT", "Employee"};

    private static final String[][] EXPECTED = {
        // Admin: everything
        {"VIEW_ALL_EMPLOYEES", "ADD_EMPLOYEE", "MODIFY_EMPLOYEE", "DELETE_EMPLOYEE",
         "CALCULATE_SALARY", "GENERATE_PAYSLIP", "APPROVE_LEAVE", "MANAGE_USER_ACCOUNTS"},
        // HR
        {"VIEW_ALL_EMPLOYEES", "ADD_EMPLOYEE", "MODIFY_EMPLOYEE", "DELETE_EMPLOYEE", "APPROVE_LEAVE"},
        // Finance
        {"VIEW_ALL_EMPLOYEES", "CALCULATE_SALARY", "GENERATE_PAYSLIP"},
        // IT
        {"VIEW_ALL_EMPLOYEES", "MANAGE_USER_ACCOUNTS"},
        // Employee
        {}
    };

    public static void main(String[] args) throws Exception {

        setupTestDataDir();

        try {
            C2_T1();
            C2_T2();
            C2_T3();
            C2_T4();
            C2_T5();
            C2_T6();
            C2_T7();
            C2_T8();
            C2_T9();
            C2_T10();
            C2_T11();
            C2_T12();
            C2_T13();

            System.out.println("\n=== TEST SUMMARY ===");
            System.out.println("Passed: " + passed);
            System.out.println("Failed: " + failed);
            System.out.println("Total: " + (passed + failed));

            if (failed > 0) {
                System.exit(1);
            }
        } finally {
            Session.clear();
            cleanupTestDataDir();
        }
    }

    // ---------- tests ----------

    private static void C2_T1() {
        test("C2-T1", "no login (no role) is denied everything", () -> {
            Session.clear();

            for (Permission p : Permission.values()) {
                if (AccessControl.can(p)) return false;
            }

            return denied(() -> new EmployeeService().getEmployees())
                && denied(() -> new LoginService().getAllUsers())
                && denied(() -> new LeaveService().getAllLeaves());
        });
    }

    private static void C2_T2() {
        test("C2-T2", "permission matrix matches the approved design (5 roles x 8 permissions)", () -> {

            for (int r = 0; r < ROLES.length; r++) {

                login(ROLES[r], "10001", "Test", "User");
                List<String> allowedNames = Arrays.asList(EXPECTED[r]);

                for (Permission p : Permission.values()) {

                    boolean shouldHave = allowedNames.contains(p.name());

                    if (AccessControl.can(p) != shouldHave) {
                        System.out.println("   mismatch: " + ROLES[r] + " / " + p
                                + " expected " + shouldHave);
                        return false;
                    }
                }
            }
            return true;
        });
    }

    private static void C2_T3() {
        test("C2-T3", "unknown or malformed roles get no permissions", () -> {

            String[] badRoles = {"Hacker", "", "Administrator", "ADMIN ", "Admin;HR", "root"};

            for (String role : badRoles) {

                login(role, "10001", "Test", "User");

                for (Permission p : Permission.values()) {
                    if (AccessControl.can(p)) {
                        System.out.println("   role '" + role + "' was granted " + p);
                        return false;
                    }
                }
            }
            return true;
        });
    }

    private static void C2_T4() {
        test("C2-T4", "Employee cannot manage user accounts (LoginService)", () -> {
            login("Employee", "10005", "Eduard", "Hernandez");
            LoginService s = new LoginService();

            return denied(() -> s.createUser("99999", "evil", "E", "V", "Admin"))
                && denied(() -> s.resetPassword("MGarcia", "Hacked@123"))
                && denied(() -> s.deleteUser("MGarcia"))
                && denied(() -> s.getAllUsers())
                && denied(() -> s.getResetRequests())
                && denied(() -> s.removeResetRequest("MGarcia"));
        });
    }

    private static void C2_T5() {
        test("C2-T5", "IT: manages accounts, but cannot touch employees, leave or payroll", () -> {
            Employee other = employeeAsAdmin("10001");

            login("IT", "10002", "Antonio", "Lim");

            LoginService ls = new LoginService();
            EmployeeService es = new EmployeeService();
            LeaveService lv = new LeaveService();

            boolean canManage = allowed(() -> ls.getAllUsers())
                             && allowed(() -> ls.getResetRequests());

            boolean blocked = denied(() -> es.updateEmployee(other))
                           && denied(() -> es.deleteEmployee("10001"))
                           && denied(() -> addEmployeeCall(es))
                           && denied(() -> lv.getAllLeaves())
                           && denied(() -> lv.approveLeave(null))
                           && denied(() -> PayrollService.calculatePayroll(other, Constants.ATTENDANCE_CSV, "All"))
                           && denied(() -> PayrollService.generatePayslip(other, Constants.ATTENDANCE_CSV, "All"));

            return canManage && blocked;
        });
    }

    private static void C2_T6() {
        test("C2-T6", "HR: employees and leave yes; accounts and payroll no", () -> {
            Employee other = employeeAsAdmin("10001");

            login("HR", "10006", "Andrea Mae", "Villanueva");

            EmployeeService es = new EmployeeService();
            LeaveService lv = new LeaveService();
            LoginService ls = new LoginService();

            boolean canDo = allowed(() -> es.getEmployees())
                         && allowed(() -> es.getEmployee("10001"))
                         && allowed(() -> lv.getAllLeaves());

            boolean blocked = denied(() -> ls.createUser("99999", "evil", "E", "V", "Admin"))
                           && denied(() -> ls.getAllUsers())
                           && denied(() -> PayrollService.calculatePayroll(other, Constants.ATTENDANCE_CSV, "All"))
                           && denied(() -> PayrollService.generatePayslip(other, Constants.ATTENDANCE_CSV, "All"));

            return canDo && blocked;
        });
    }

    private static void C2_T7() {
        test("C2-T7", "Finance: payroll and payslips yes; edit/delete, leave and accounts no", () -> {
            Employee other = employeeAsAdmin("10001");

            login("Finance", "10007", "Bianca Sofia", "Aquino");

            EmployeeService es = new EmployeeService();
            LeaveService lv = new LeaveService();
            LoginService ls = new LoginService();

            boolean canDo = allowed(() -> es.getEmployees())
                         && allowed(() -> PayrollService.calculatePayroll(other, Constants.ATTENDANCE_CSV, "All"))
                         && allowed(() -> PayrollService.generatePayslip(other, Constants.ATTENDANCE_CSV, "All"));

            boolean blocked = denied(() -> es.updateEmployee(other))
                           && denied(() -> es.deleteEmployee("10001"))
                           && denied(() -> lv.getAllLeaves())
                           && denied(() -> lv.rejectLeave(null))
                           && denied(() -> ls.getAllUsers());

            return canDo && blocked;
        });
    }

    private static void C2_T8() {
        test("C2-T8", "Employee can only reach their own record (employee, attendance, payroll, payslip)", () -> {
            Employee own = employeeAsAdmin("10003");
            Employee other = employeeAsAdmin("10004");

            login("Employee", "10003", "Bianca Sofia", "Aquino");

            EmployeeService es = new EmployeeService();
            AttendanceService as = new AttendanceService();

            boolean ownOk = allowed(() -> es.getEmployee("10003"))
                         && allowed(() -> as.getAttendance("10003"))
                         && allowed(() -> as.getAvailableMonths("10003"))
                         && allowed(() -> PayrollService.calculatePayroll(own, Constants.ATTENDANCE_CSV, "All"))
                         && allowed(() -> PayrollService.generatePayslip(own, Constants.ATTENDANCE_CSV, "All"));

            boolean otherBlocked = denied(() -> es.getEmployee("10004"))
                                && denied(() -> es.getEmployees())
                                && denied(() -> as.getAttendance("10004"))
                                && denied(() -> as.getAvailableMonths("10004"))
                                && denied(() -> as.checkIn("10004", "Reyes", "Isabella"))
                                && denied(() -> as.checkOut("10004"))
                                && denied(() -> PayrollService.calculatePayroll(other, Constants.ATTENDANCE_CSV, "All"))
                                && denied(() -> PayrollService.generatePayslip(other, Constants.ATTENDANCE_CSV, "All"));

            return ownOk && otherBlocked;
        });
    }

    private static void C2_T9() {
        test("C2-T9", "Leave: employees see/submit only their own; approving needs APPROVE_LEAVE", () -> {
            login("Employee", "10008", "Beatriz", "Santos");

            LeaveService lv = new LeaveService();

            boolean ownOk = allowed(() -> lv.getLeavesByEmployee("Beatriz Santos"));

            boolean otherBlocked =
                    denied(() -> lv.getLeavesByEmployee("Manuel III Garcia"))
                 && denied(() -> lv.submitLeave("Manuel III Garcia", "Vacation Leave",
                            LocalDate.now(), LocalDate.now().plusDays(1), "not mine"))
                 && denied(() -> lv.getAllLeaves())
                 && denied(() -> lv.approveLeave(null))
                 && denied(() -> lv.rejectLeave(null));

            return ownOk && otherBlocked;
        });
    }

    private static void C2_T10() {
        test("C2-T10", "Admin has full access", () -> {
            login("Admin", "10001", "Manuel III", "Garcia");

            for (Permission p : Permission.values()) {
                if (!AccessControl.can(p)) return false;
            }

            return allowed(() -> new EmployeeService().getEmployees())
                && allowed(() -> new LoginService().getAllUsers())
                && allowed(() -> new LoginService().getResetRequests())
                && allowed(() -> new LeaveService().getAllLeaves());
        });
    }

    private static void C2_T11() {
        test("C2-T11", "denied attempts are written to audit.log (and allowed ones are not)", () -> {
            File log = new File(testDataDir, "audit.log");
            if (log.exists()) log.delete();

            // allowed action first: must not create a log line
            login("Admin", "10001", "Manuel III", "Garcia");
            new EmployeeService().getEmployees();
            if (log.exists() && Files.readAllLines(log.toPath()).size() > 0) return false;

            // denied action: must be logged with who, role and what
            login("Employee", "10005", "Eduard", "Hernandez");
            denied(() -> new EmployeeService().getEmployees());

            if (!log.exists()) return false;

            List<String> lines = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);
            if (lines.size() != 1) return false;

            String line = lines.get(0);

            return line.contains("DENIED")
                && line.contains("empNo=10005")
                && line.contains("role=Employee")
                && line.contains("permission=VIEW_ALL_EMPLOYEES")
                && !line.toLowerCase().contains("password");
        });
    }

    private static void C2_T12() {
        test("C2-T12", "logging out (Session.clear) removes access again", () -> {
            login("Admin", "10001", "Manuel III", "Garcia");

            boolean before = allowed(() -> new LoginService().getAllUsers());

            Session.clear();

            boolean after = denied(() -> new LoginService().getAllUsers())
                         && denied(() -> new EmployeeService().getEmployees());

            return before && after;
        });
    }

    private static void C2_T13() {
        test("C2-T13", "login and password flows still work with no session (Control 1 not blocked)", () -> {
            Session.clear();

            LoginService s = new LoginService();

            // these run BEFORE login, so RBAC must not block them
            boolean wrongPw = s.authenticate("MGarcia", "WrongPassword123!") == null;
            s.isDefaultPassword("MGarcia");   // must simply run without being blocked
            boolean changeFails = !s.changeOwnPassword("MGarcia", "WrongPass", "Newpass1!");
            boolean userLookup = s.getUserByUsername("MGarcia") != null;

            s.addResetRequest("MGarcia");   // "Forgot Password?" must stay open

            return wrongPw && changeFails && userLookup;
        });
    }

    // ---------- helpers ----------

    private static void login(String role, String empNo, String firstName, String lastName) {
        Session.role = role;
        Session.empNo = empNo;
        Session.firstName = firstName;
        Session.lastName = lastName;
    }

    // read an employee record using an Admin session, then leave the session cleared
    private static Employee employeeAsAdmin(String empNo) {
        login("Admin", "10001", "Manuel III", "Garcia");
        Employee e = new EmployeeService().getEmployee(empNo);
        Session.clear();
        return e;
    }

    private static void addEmployeeCall(EmployeeService es) {
        es.addEmployee("99999", "Test", "User", "Regular", "HR Rank and File", "San, Jose Brad",
                "01/01/2000", "Address", "000-000-000", "00-0000000-0", "000000000000",
                "000-000-000-000", "000000000000", "20000", "1500", "1000", "800",
                "23300", "113.64");
    }

    @FunctionalInterface
    private interface Call {
        void run() throws Exception;
    }

    // true when the call is refused by RBAC
    private static boolean denied(Call c) {
        try {
            c.run();
            return false;
        } catch (AccessDeniedException e) {
            return true;
        } catch (Exception e) {
            return false;   // some other error: authorization itself was not the problem
        }
    }

    // true when RBAC lets the call through
    private static boolean allowed(Call c) {
        return !denied(c);
    }

    private static void setupTestDataDir() throws IOException {
        testDataDir = Files.createTempDirectory("motorph-rbac-test-").toString();
        System.setProperty("motorph.dataDir", testDataDir);

        File srcResources = new File("src/motorph_oop/resources");
        File destResources = new File(testDataDir);

        for (File file : srcResources.listFiles()) {
            Files.copy(file.toPath(), new File(destResources, file.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        }

        System.out.println("Test data directory: " + testDataDir);
    }

    private static void cleanupTestDataDir() throws IOException {
        if (testDataDir != null) {
            deleteDirectory(new File(testDataDir));
        }
    }

    private static void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        Files.delete(dir.toPath());
    }

    private static void test(String id, String description, TestRunnable test) {
        try {
            boolean result = test.run();
            if (result) {
                System.out.println("[PASS] " + id + " " + description);
                passed++;
            } else {
                System.out.println("[FAIL] " + id + " " + description + " expected=true actual=false");
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[FAIL] " + id + " " + description + " exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            Session.clear();
        }
    }

    @FunctionalInterface
    private interface TestRunnable {
        boolean run() throws Exception;
    }
}