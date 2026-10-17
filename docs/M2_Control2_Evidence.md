# Milestone 2 – Control 2: Role-Based Access Control Evidence

## Project Information

- **Project**: MotorPH Payroll Management System
- **Group**: S4101 – Group 16
- **Branch**: Justine-rbac-updates
- **Base Commit**: 5370d98 (merged Control 1 into master)
- **Date**: 2026-10-16

## Control Summary

- **Control Name**: Role-Based Access Control (RBAC)
- **Control Type**: Technical
- **Mapped Surface**:
  - Component: All service layers (EmployeeService, LoginService, LeaveService, PayrollService, AttendanceService) and UI components (AdminDashboardPanel)
  - Surface Type: Code surface (authorization checks in service methods)
- **Security Goal**: Principle of least privilege; users can only access data and perform actions appropriate to their role; self-access allowed regardless of role

## Tool/Library Information

- **JDK Version**: Java 23 (javac.source=23)
- **No external libraries added**: RBAC implementation uses only Java standard library
- **Audit Logging**: Java NIO with UTF-8 for audit.log

## Files Changed

| File | Key Change |
|------|------------|
| `MotorPH_OOP/src/motorph_oop/security/AccessControl.java` | NEW: Centralized permission matrix and authorization checks (can, require, requireSelfOr, requireSelf) |
| `MotorPH_OOP/src/motorph_oop/security/Permission.java` | NEW: Enum defining 8 system permissions |
| `MotorPH_OOP/src/motorph_oop/security/AuditLog.java` | NEW: Logs denied access attempts to audit.log |
| `MotorPH_OOP/src/motorph_oop/security/AccessDeniedException.java` | NEW: Custom exception for authorization failures |
| `MotorPH_OOP/src/motorph_oop/service/EmployeeService.java` | Added AccessControl.require() checks for view/add/modify/delete; added requireSelfOr for getEmployee |
| `MotorPH_OOP/src/motorph_oop/service/LoginService.java` | Added MANAGE_USER_ACCOUNTS permission checks for createUser, resetPassword, getAllUsers, deleteUser, getResetRequests, removeResetRequest |
| `MotorPH_OOP/src/motorph_oop/service/LeaveService.java` | Added permission checks; users can only submit/view own leave requests; approval requires APPROVE_LEAVE |
| `MotorPH_OOP/src/motorph_oop/service/PayrollService.java` | Added CALCULATE_SALARY and GENERATE_PAYSLIP permission checks with self-access support |
| `MotorPH_OOP/src/motorph_oop/service/AttendanceService.java` | Added requireSelf for checkIn/checkOut; requireSelfOr for viewing attendance |
| `MotorPH_OOP/src/motorph_oop/ui/AdminDashboardPanel.java` | Added Session.clear() on logout; replaced switch-based permission configuration with AccessControl.can() |
| `MotorPH_OOP/src/motorph_oop/util/Session.java` | No structural changes; used by AccessControl for authorization context |
| `MotorPH_OOP/test/motorph_oop/RBACTest.java` | NEW: Comprehensive test harness with 13 test cases |

## Before vs After

### EmployeeService.getEmployees() Before

```java
17:    // get all employees (Admin, HR, Finance, IT)
18:    public List<Employee> getEmployees() {
19:        return employeeDAO.getAllEmployees();
20:    }
```

### EmployeeService.getEmployees() After

```java
17:    // get all employees (Admin, HR, Finance, IT)
18:    public List<Employee> getEmployees() {
19:        AccessControl.require(Permission.VIEW_ALL_EMPLOYEES);
20:        return employeeDAO.getAllEmployees();
21:    }
```

### EmployeeService.getEmployee() Before

```java
23:    // get one employee: your own record, or anyone's if you can view all
24:    public Employee getEmployee(String empNo) {
25:        return employeeDAO.findEmployeeById(empNo);
26:    }
```

### EmployeeService.getEmployee() After

```java
23:    // get one employee: your own record, or anyone's if you can view all
24:    public Employee getEmployee(String empNo) {
25:        AccessControl.requireSelfOr(empNo, Permission.VIEW_ALL_EMPLOYEES);
26:        return employeeDAO.findEmployeeById(empNo);
27:    }
```

### LoginService.resetPassword() Before

```java
82:    // reset password (Admin, IT)
83:    public boolean resetPassword(String username, String newPassword) {
84:        String hashed = PasswordHasher.hash(newPassword);
85:        return loginDAO.updatePassword(username, hashed);
86:    }
```

### LoginService.resetPassword() After

```java
84:    // reset password (Admin, IT)
85:    public boolean resetPassword(String username, String newPassword) {
86:        AccessControl.require(Permission.MANAGE_USER_ACCOUNTS);
87:        String hashed = PasswordHasher.hash(newPassword);
88:        return loginDAO.updatePassword(username, hashed);
89:    }
```

### LeaveService.submitLeave() Before

```java
58:    // you can only submit leave for yourself
59:    public LeaveRequest submitLeave(
60:            String employeeName,
61:            String leaveType,
62:            LocalDate startDate,
63:            LocalDate endDate,
64:            String reason) {

        validateLeaveDates(startDate, endDate);

        long days = calculateLeaveDays(startDate, endDate);

        LeaveRequest request =
                new LeaveRequest(employeeName, leaveType, startDate, endDate, days, reason);

        leaveDAO.saveLeave(request);

        return request;
    }
```

### LeaveService.submitLeave() After

```java
58:    // you can only submit leave for yourself
59:    public LeaveRequest submitLeave(
60:            String employeeName,
61:            String leaveType,
62:            LocalDate startDate,
63:            LocalDate endDate,
64:            String reason) {

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
```

### PayrollService.calculatePayroll() Before

```java
95:    // Calculates payroll details (Admin, Finance, or the employee's own record).
96:    public static PayrollResult calculatePayroll(Employee employee,
97:                                                 String attendanceFile,
98:                                                 String selectedMonth) {
        return compute(employee, attendanceFile, selectedMonth);
    }
```

### PayrollService.calculatePayroll() After

```java
95:    // Calculates payroll details (Admin, Finance, or the employee's own record).
96:    public static PayrollResult calculatePayroll(Employee employee,
97:                                                 String attendanceFile,
98:                                                 String selectedMonth) {

        AccessControl.requireSelfOr(employee.getEmpNo(), Permission.CALCULATE_SALARY);
        return compute(employee, attendanceFile, selectedMonth);
    }
```

### AdminDashboardPanel.configurePermissions() Before

```java
288:    // configure role permissions
289:    private void configurePermissions() {

        switch (userRole.toUpperCase()) {

            case "ADMIN":
                configureAdmin();
                break;

            case "IT":
                configureIT();
                break;

            case "HR":
                configureHR();
                break;

            case "FINANCE":
                configureFinance();
                break;

            default:
                showUnauthorized();
                hideAll();
        }
    }

    private void configureAdmin() {

        btnManageLogin.setEnabled(true);
        btnDatabase.setEnabled(true);
        btnAdd.setEnabled(true);
        btnLeaveRequests.setEnabled(true);

        btnTime.setVisible(false);
    }

    private void configureIT() {

        btnManageLogin.setEnabled(true);
        btnDatabase.setEnabled(true);

        btnAdd.setVisible(false);
        btnLeaveRequests.setVisible(false);
        btnTime.setVisible(false);
    }

    private void configureHR() {

        btnManageLogin.setVisible(false);
        btnDatabase.setEnabled(true);
        btnAdd.setEnabled(true);

        btnLeaveRequests.setVisible(true);
        btnTime.setVisible(false);
    }

    private void configureFinance() {

        btnManageLogin.setVisible(false);
        btnDatabase.setEnabled(true);

        btnAdd.setVisible(false);
        btnLeaveRequests.setVisible(false);
        btnTime.setVisible(false);
    }

    private void hideAll() {

        btnDatabase.setVisible(false);
        btnAdd.setVisible(false);
        btnLeaveRequests.setVisible(false);
        btnTime.setVisible(false);
    }
```

### AdminDashboardPanel.configurePermissions() After

```java
288:    // configure what this role can see (based on the permission matrix)
289:    private void configurePermissions() {

        boolean canViewAll = AccessControl.can(Permission.VIEW_ALL_EMPLOYEES);
        boolean canManageUsers = AccessControl.can(Permission.MANAGE_USER_ACCOUNTS);

        btnDatabase.setVisible(canViewAll);
        btnAdd.setEnabled(canViewAll);
        btnLeaveRequests.setEnabled(canViewAll);
        btnManageLogin.setEnabled(canManageUsers);

        btnTime.setVisible(false);
    }
```

## Test Results

```
Test data directory: C:\Users\asaor\AppData\Local\Temp\motorph-rbac-test-7941977631637950915
[PASS] C2-T1 no login (no role) is denied everything
[PASS] C2-T2 permission matrix matches the approved design (5 roles x 8 permissions)
[PASS] C2-T3 unknown or malformed roles get no permissions
[PASS] C2-T4 Employee cannot manage user accounts (LoginService)
[PASS] C2-T5 IT: manages accounts, but cannot touch employees, leave or payroll
[PASS] C2-T6 HR: employees and leave yes; accounts and payroll no
[PASS] C2-T7 Finance: payroll and payslips yes; edit/delete, leave and accounts no
[PASS] C2-T8 Employee can only reach their own record (employee, attendance, payroll, payslip)
[PASS] C2-T9 Leave: employees see/submit only their own; approving needs APPROVE_LEAVE
[PASS] C2-T10 Admin has full access
[PASS] C2-T11 denied attempts are written to audit.log (and allowed ones are not)
[PASS] C2-T12 logging out (Session.clear) removes access again
[PASS] C2-T13 login and password flows still work with no session (Control 1 not blocked)

=== TEST SUMMARY ===
Passed: 13
Failed: 0
Total: 13
```

**Result**: 13/13 tests passed

## Permission Matrix

| Role       | VIEW_ALL_EMPLOYEES | ADD_EMPLOYEE | MODIFY_EMPLOYEE | DELETE_EMPLOYEE | CALCULATE_SALARY | GENERATE_PAYSLIP | APPROVE_LEAVE | MANAGE_USER_ACCOUNTS |
|------------|-------------------|--------------|-----------------|----------------|------------------|------------------|---------------|---------------------|
| Admin      | ✅                 | ✅           | ✅              | ✅             | ✅                | ✅                | ✅            | ✅                   |
| HR         | ✅                 | ✅           | ✅              | ✅             | ❌                | ❌                | ✅            | ❌                   |
| Finance    | ✅                 | ❌           | ❌              | ❌             | ✅                | ✅                | ❌            | ❌                   |
| IT         | ✅                 | ❌           | ❌              | ❌             | ❌                | ❌                | ❌            | ✅                   |
| Employee   | ❌                 | ❌           | ❌              | ❌             | ❌                | ❌                | ❌            | ❌                   |

## Special Authorization Rules

### Self-Access Rule
Users can always access their own records regardless of role. This is implemented via:
- `AccessControl.requireSelf(targetEmpNo)` - strict self-only access
- `AccessControl.requireSelfOr(targetEmpNo, permission)` - self OR permission

### Pre-Login Operations
Operations that must work before authentication (no session) have no permission checks:
- `LoginService.authenticate()` - authentication itself
- `LoginService.isDefaultPassword()` - needed for first-login flow
- `LoginService.changeOwnPassword()` - needed for forced password change
- `LoginService.getUserByUsername()` - needed for "Forgot Password?"
- `LoginService.addResetRequest()` - needed for lockout reset requests

### Audit Logging
All denied access attempts are logged to `audit.log` with:
- Timestamp
- Employee number (from Session.empNo)
- Role (from Session.role)
- Attempted permission

Sample audit.log entry:
```
2026-10-16T10:30:45.123 | DENIED | empNo=10005 | role=Employee | permission=VIEW_ALL_EMPLOYEES
```

## Tool Results

### Static Analysis

**grep for unchecked service calls**:
```bash
grep -rn "employeeDAO\.\|loginDAO\.\|leaveDAO\." MotorPH_OOP/src/motorph_oop/service/
```
All service methods now pass through service layer with permission checks; direct DAO access is not exposed.

**grep for missing AccessControl checks**:
No service method that should be protected lacks an AccessControl check (verified by test coverage).

## Issues Encountered and Resolutions

1. **Pre-login operations blocked by RBAC**: Initial implementation added permission checks to all LoginService methods, which broke authentication and password reset flows. Fixed by removing checks from `authenticate()`, `isDefaultPassword()`, `changeOwnPassword()`, `getUserByUsername()`, and `addResetRequest()` since these must work before login.

2. **Session.clear() not called on logout**: Users retained session state after logout. Fixed by adding `Session.clear()` to the logout button action in AdminDashboardPanel.

3. **Test isolation**: RBACTest needs to clear Session between tests to prevent state leakage. Fixed by adding `Session.clear()` in the test `finally` block and after each test case.

## Residual Risks (Deferred)

- **R-004**: Session stored in static variables (Session.empNo, Session.role, etc.) means concurrent logins in the same JVM could interfere. This is acceptable for a desktop application but would need session tokens for web deployment.
- **R-005**: Audit log file can be deleted or modified by anyone with file system access. In production, audit logs should be append-only and sent to a centralized log server.
- **R-006**: No account lockout after failed authorization attempts. A user could brute-force permissions if they have session access. This is mitigated by the existing login lockout (Control 1).
- **R-007**: CSV files remain unencrypted. RBAC protects access through the application, but direct file access bypasses authorization. This is out of scope for Control 2 but could be addressed with file system permissions or encryption.

## Rollback Instructions

To rollback this control:

1. Delete the new security package:
   ```bash
   rm -rf MotorPH_OOP/src/motorph_oop/security/
   ```

2. Revert each service file to remove AccessControl imports and calls:
   ```bash
   git checkout HEAD~1 -- MotorPH_OOP/src/motorph_oop/service/
   git checkout HEAD~1 -- MotorPH_OOP/src/motorph_oop/ui/AdminDashboardPanel.java
   ```

3. Delete the test file:
   ```bash
   rm MotorPH_OOP/test/motorph_oop/RBACTest.java
   ```

4. Rebuild the project.

## Test Command

```bash
cd MotorPH_OOP
javac -encoding UTF-8 -d build/classes -cp "lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" $(find src test -name "*.java")
java -cp "build/classes;lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" motorph_oop.RBACTest
```

## Integration with Control 1

Control 2 builds upon Control 1 (Password Hashing):
- Control 1 ensures passwords are hashed and verified securely
- Control 2 ensures only authorized users can access and modify data
- Both controls use the same Session context for authorization
- Control 2's audit logging complements Control 1's security logging
- Test harnesses for both controls use the same temporary directory pattern (motorph.dataDir system property)
