# <img src="https://github.com/TheDudeThatCode/TheDudeThatCode/blob/master/Assets/Hi.gif" width="35" /> HELLO, WORLD!





<p align="center">
  <img width="2245" height="1587" alt="objection" src="https://github.com/user-attachments/assets/93d95ddc-4908-45f2-ac36-22a35c6aacda" />
</p>

<h2 align="center"> <img src="https://github.com/TheDudeThatCode/TheDudeThatCode/blob/master/Assets/Developer.gif" width="45" /> About Us </h2>

This project is the MotorPH Payroll Management System, originally developed for the Object-Oriented Programming course (MO-IT103) by Group 6, and now being used for the Information Assurance and Security 2 course (MO-IT153) by Group 16.

**Code Origin & Permission**: The original codebase was graciously shared by Group 6 (Jriv7929/Group-6_OOP_-A2101) with permission for use in the IAS1 course. For IAS2, we are building upon this foundation to implement security controls as part of the Milestone 2 deliverables.

**Current Focus (IAS2 - MO-IT153)**: Our team (S4101 – Group 16) is implementing technical security controls to address vulnerabilities in the MotorPH system, starting with password hashing and secure verification using bcrypt, followed by role-based access control.

---

## ✨ Key Features

- 🧩 Modular class-based payroll architecture
- 🔐 Role-based user hierarchy (Admin, HR, IT, Logistics, etc.)
- 📦 Separation of concerns
- 🔄 Inheritance-driven design
- 📊 Payroll computation engine
- 🧪 Academic architecture documentation (CRC Cards, UML, etc.)
- 🔒 Security controls implementation (IAS2)

Designed not just as a requirement — but as a developer growth milestone

---
<!-- OOP CONCEPTS -->
<div align="center">

<h2>🧠 OOP Concepts Applied</h2>

<table>
<tr>
<th>🧩 Concept</th>
<th>🚀 Implementation</th>
</tr>
<tr>
<td><b>Encapsulation</b></td>
<td>Sensitive payroll data encapsulated within domain classes</td>
</tr>
<tr>
<td><b>Inheritance</b></td>
<td>Role-based hierarchy extending a shared SystemUser base class</td>
</tr>
<tr>
<td><b>Polymorphism</b></td>
<td>Department-specific behaviors overriding base methods</td>
</tr>
<tr>
<td><b>Abstraction</b></td>
<td>Abstract parent classes defining shared system contracts</td>
</tr>
</table>

</div>

---

## 📈 Current Project Progress

**IAS1 (MO-IT103)**: ✅ Complete
- OOP architecture implementation
- Payroll computation engine
- Role-based access control structure

**IAS2 (MO-IT153)**: 🔄 In Progress
- Milestone 2 – Control 1: Password Hashing ✅ Complete
- Milestone 2 – Control 2: Role-Based Access Control ✅ Complete

---

## Milestone 2 – Control 1: Password Hashing

Implemented bcrypt password hashing using jBCrypt 0.4 with cost factor 12 to replace plaintext password storage and comparison.

### Key Changes
- All passwords in `MotorPH_EmployeeLogin.csv` are now stored as bcrypt hashes
- Authentication uses `PasswordHasher.verify()` instead of plaintext `.equals()`
- `LoginDAO` guards against non-bcrypt password writes
- Forced first-login password change no longer allows users to reset other users' passwords
- Hardcoded `"1234"` literals replaced with `LoginService.TEMP_PASSWORD` constant

### Test Command
```bash
cd MotorPH_OOP
javac -encoding UTF-8 -d build/classes -cp "lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" $(find src test -name "*.java")
java -cp "build/classes;lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" motorph_oop.PasswordHashingTest
```

### Migration Process and Error Handling

The password migration tool performs these steps:
1. Checks that the login CSV exists
2. Creates `backup\MotorPH_EmployeeLogin.plaintext.bak.csv`
3. Reads the CSV
4. Keeps rows that already contain a bcrypt hash
5. Hashes old password values with bcrypt cost 12
6. Reports migrated, skipped, and malformed rows
7. Writes the rows back to the login CSV

**Migration Verification**: All 33 old passwords verified successfully against their new bcrypt hashes (see [docs/evidence/after/migration-check.txt](docs/evidence/after/migration-check.txt)).

**Error Handling**:
| Case | Current result | Required response |
|---|---|---|
| Login CSV is missing | Tool prints error and exits | Check path and restore approved backup |
| Login CSV is empty | Tool creates backup, prints error, exits | Restore last good file |
| Row has fewer than six fields | Tool prints full row and skips it | Stop migration; repair row; run checks again |
| Value is already a bcrypt hash | Tool skips it | Check it belongs to correct account |
| Read, hash, or write error | Error reaches console | Keep backup; restore last good file; check before retry |
| Damaged hash | `verify()` returns `false` | Reset account through approved account flow |

**Rollback and Recovery**:
If migration fails:
1. Stop the application
2. Stop all writes to the login CSV
3. Keep the failed file and error output for review
4. Do not overwrite the backup
5. Restore the backup:
   ```bash
   copy /Y backup\MotorPH_EmployeeLogin.plaintext.bak.csv src\motorph_oop\resources\MotorPH_EmployeeLogin.csv
   ```
6. Do not run bcrypt-only code against restored plain passwords
7. Check header, row count, and hash format
8. Run PasswordHashingTest
9. Change any password exposed in the backup

### Test Procedures and Expected Results

**Password Hashing Tests**:
| Test | Procedure | Expected Result |
|---|---|---|
| Correct password | Hash a known test password and authenticate with it | Authentication succeeds |
| Wrong password | Authenticate with a different password | Authentication returns no user |
| Stored data | Inspect all 33 login rows with `isBcryptHash` | 33/33 are bcrypt hashes |
| Salt uniqueness | Hash the same input twice | Hash strings differ and both verify |
| Hash-as-password | Submit the stored hash as the password | Rejected |
| Cost | Inspect the bcrypt cost marker | Cost is 12 |

**Malformed Inputs and Damaged Hashes**:
- Call `PasswordHasher.verify()` with null plaintext, null hash, empty strings, plaintext, malformed bcrypt text, and damaged bcrypt string
- Call `PasswordHasher.hash()` with null and empty input
- **Expected results**: `verify()` returns `false` without accepting the input; `hash()` rejects null/empty input with `IllegalArgumentException`; no plaintext is written by `LoginDAO`
- **Known gap**: Direct `LoginService.authenticate(null, ...)` causes an uncaught `NullPointerException` in `LoginDAO.findUserByUsername`

**Test Result**: 11/11 tests passed

### Static Analysis Findings

**Semgrep**:
- 60 Java rules: **0 findings**
- Hardcoded "1234" rule: **4 findings** (all at the intentional `LoginService.TEMP_PASSWORD` constant or its uses)
- This is a documented residual risk, not a clean security result

**SpotBugs**:
- 85 warnings after Control 1, including:
  - Default-encoding warnings in non-login CSV/payroll code
  - Unchecked directory creation in the migration tool
- These are not silently treated as resolved

### Remaining Limitations and Untested Scenarios

The following items are intentionally not described as completed controls:

- The shared temporary password `1234` remains until a secure reset control is implemented; credential rotation is required for real deployment
- The null-username authentication path throws instead of returning a normal authentication failure
- Username timing attack vulnerability (existing average: 206.100ms vs nonexistent: 0.060ms, ratio: 3,435:1)
- The migration tool has no atomic write, automatic rollback, or concurrency lock
- Static `Session` fields are not safe for concurrent users in one JVM
- CSV files and the audit log are not encrypted; filesystem access bypasses application RBAC
- The audit log is local and tamperable without central collection
- A malformed migration row is currently printed in full to stderr; a malformed row could expose a legacy password to console history
- The tests do not provide: production multi-process migration test, crash/power-loss recovery test, filesystem-permission test, backup confidentiality test, log rotation/retention test, or full UI end-to-end authorization test
- The tests do not establish resistance to denial-of-service, password spraying, or network interception (desktop CSV application)

### Evidence
See [docs/M2_Control1_Evidence.md](docs/M2_Control1_Evidence.md) for detailed evidence, test results, and before/after comparisons.

For complete migration details, stress test results, and additional documented limitations, see [docs/Security_Controls_Runbook.md](docs/Security_Controls_Runbook.md).

---

## Milestone 2 – Control 2: Role-Based Access Control (RBAC)

Implemented a comprehensive RBAC system with permission-based authorization checks across all service layers, ensuring users can only access data and perform actions appropriate to their role.

### Key Changes
- **New Security Package**: Created `motorph_oop.security` package with:
  - `AccessControl.java` - Centralized permission matrix and authorization checks
  - `Permission.java` - Enum defining 8 system permissions (VIEW_ALL_EMPLOYEES, ADD_EMPLOYEE, MODIFY_EMPLOYEE, DELETE_EMPLOYEE, CALCULATE_SALARY, GENERATE_PAYSLIP, APPROVE_LEAVE, MANAGE_USER_ACCOUNTS)
  - `AuditLog.java` - Logs denied access attempts to audit.log
  - `AccessDeniedException.java` - Custom exception for authorization failures

- **Service Layer Authorization**:
  - `EmployeeService` - Permission checks for view/add/modify/delete operations; users can access their own records
  - `LoginService` - User account management restricted to Admin/IT (MANAGE_USER_ACCOUNTS)
  - `LeaveService` - Users can only submit/view their own leave requests; approval restricted to HR/Admin
  - `PayrollService` - Payroll calculation and payslip generation restricted to Finance/Admin or own record
  - `AttendanceService` - Check-in/out restricted to own record; viewing requires VIEW_ALL_EMPLOYEES or self

- **Session Management**:
  - `Session.clear()` called on logout to invalidate user session
  - Authorization checks rely on Session.empNo, Session.role, Session.firstName, Session.lastName

- **UI Updates**:
  - `AdminDashboardPanel` - Simplified permission configuration using AccessControl.can() instead of switch statements
  - All UI buttons now controlled by permission checks

### Permission Matrix
| Role       | VIEW_ALL_EMPLOYEES | ADD_EMPLOYEE | MODIFY_EMPLOYEE | DELETE_EMPLOYEE | CALCULATE_SALARY | GENERATE_PAYSLIP | APPROVE_LEAVE | MANAGE_USER_ACCOUNTS |
|------------|-------------------|--------------|-----------------|----------------|------------------|------------------|---------------|---------------------|
| Admin      | ✅                 | ✅           | ✅              | ✅             | ✅                | ✅                | ✅            | ✅                   |
| HR         | ✅                 | ✅           | ✅              | ✅             | ❌                | ❌                | ✅            | ❌                   |
| Finance    | ✅                 | ❌           | ❌              | ❌             | ✅                | ✅                | ❌            | ❌                   |
| IT         | ✅                 | ❌           | ❌              | ❌             | ❌                | ❌                | ❌            | ✅                   |
| Employee   | ❌                 | ❌           | ❌              | ❌             | ❌                | ❌                | ❌            | ❌                   |

### Special Rules
- **Self-Access**: Users can always access their own records (employee data, attendance, payroll, payslips) regardless of role
- **Pre-Login Operations**: Authentication, password change, and "Forgot Password" work without permission checks (required for login flow)
- **Audit Logging**: All denied access attempts are logged with timestamp, employee number, role, and attempted permission

### Test Command
```bash
cd MotorPH_OOP
javac -encoding UTF-8 -d build/classes -cp "lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" $(find src test -name "*.java")
java -cp "build/classes;lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" motorph_oop.RBACTest
```

### RBAC Enforcement Details

Authorization is enforced at the **service boundary**, not only by hiding UI controls. The `AccessControl` class is the single permission matrix. Each protected service operation calls `require`, `requireSelfOr`, or `requireSelf`; a failed check writes a denial event and throws `AccessDeniedException`. The UI uses `AccessControl.can()` only to show or enable controls and is not the security boundary.

**Unauthorized Access Test Evidence**:
- **Horizontal access test**: Requested 34 employee records, allowed only the caller's own record (allowed=1, denied=33)
- **Denial flood test**: 5,000 calls from 20 threads resulted in denied=5000, allowed=0, errors=0; all 5,000 audit lines were well-formed and not interleaved
- **Stress test**: 50 repeated calls denied for each restricted operation, with no CSV changes

**Expected result for every unauthorized read or modification**:
1. No restricted record is returned or changed
2. `AccessDeniedException` is raised
3. One well-formed `DENIED` audit entry is appended

### Audit Logging and Sensitive Data Protection

Denied events are written as UTF-8 lines to `audit.log` in the application data directory.

**Sample audit log entry**:
```
2026-10-20T19:03:58.563994700 | DENIED | empNo=10020 | role=Employee | permission=VIEW_ALL_EMPLOYEES
```

**Sensitive Data Protection**:
The log records timestamp, employee number, role, and permission only. It does **not** record:
- Passwords
- Password hashes
- Request bodies
- Payroll values
- Plaintext employee details

**Note**: This is protection against accidental credential disclosure, not a complete log-security solution. Anyone with filesystem access can read, delete, or modify the current log. Production deployment should use restrictive file permissions, append-only or centralized log storage, controlled retention, and monitoring for tampering.

### Test Coverage
- C2-T1: No login (no role) is denied everything
- C2-T2: Permission matrix matches approved design (5 roles × 8 permissions)
- C2-T3: Unknown or malformed roles get no permissions
- C2-T4: Employee cannot manage user accounts
- C2-T5: IT manages accounts but cannot touch employees, leave, or payroll
- C2-T6: HR can manage employees and leave but not accounts or payroll
- C2-T7: Finance can access payroll/payslips but not edit/delete, leave, or accounts
- C2-T8: Employee can only reach their own records
- C2-T9: Leave submission/viewing restricted to self; approval requires APPROVE_LEAVE
- C2-T10: Admin has full access
- C2-T11: Denied attempts are written to audit.log; allowed actions are not
- C2-T12: Logging out (Session.clear) removes access
- C2-T13: Login and password flows still work without session (Control 1 not blocked)

**Test Result**: 13/13 tests passed

### Evidence
See [docs/M2_Control2_Evidence.md](docs/M2_Control2_Evidence.md) for detailed evidence, test results, and before/after comparisons.

For complete RBAC enforcement details, audit-log examples, unauthorized-access evidence, detailed test procedures, static-analysis interpretation, and remaining untested scenarios, see [docs/Security_Controls_Runbook.md](docs/Security_Controls_Runbook.md).

---

## 🚀 Get Started: Usage Instructions

Get started with Payroll Hub in just a few steps:

1️⃣ **Clone this repository**

- git clone https://github.com/angelabove00/IAS2_Group16.git

2️⃣ **Open in NetBeans**

- Open NetBeans and select `File > Open Project`.
- Navigate to the cloned repository directory and open the project.
- Ensure the required libraries are added to the project's libraries.

3️⃣ **Library Setup (IMPORTANT!)**

Before running the project, make sure the following libraries are added:

✅ **a. JFileChooser (Built-in)**
- No installation required.
- JFileChooser is part of Java Swing (javax.swing package).
- Just import it in your code: **Run Payroll Hub**

✅ **b. LGoodDatePicker (Date Picker UI) and iTextPDF (Generation)**

Step 1: Download the library

- You can see it on the github lib folder and download it.

Step 2: Add to NetBeans

- Right-click your project → Properties
- Go to Libraries
- Click Add JAR/Folder
- Select the downloaded LGoodDatePicker.jar and itextpdf.jar

✅ **c. jBCrypt (Password Hashing - IAS2)**

- jbcrypt-0.4.jar is included in the lib folder
- Automatically added to the project classpath via nbproject/project.properties

4️⃣ Run Payroll Hub
- Ensure the CSV files are in the `src/motorph_oop/resources/` directory with the required data.
- Right-click `MotorPH_OOP.java` in the `Projects` tab and select `Run File`.
- Follow the prompts to enter the employee ID, pay period, hours worked, overtime, tardiness, days late, and holiday work details.
- View the payroll statement in the console and check the CSV files for saved data.
- **Note**: Ensure CSV files are not open in another application (e.g., Excel) while running the program, as this may cause a "file in use" error when saving data.

5️⃣ **Log in credentials**

**Employee**
- User ID: BSantos
- Password: MMDC1234!

**Admin**
- User ID: MGarcia
- Password: MMDC1234!

**HR**
- User ID: Cmata
- Password: MMDC1234!

**IT**
- User ID: Ehernandez
- Password: MMDC1234!

**Finance**
- User ID: Jlopez
- Password: MMDC1234!

---

### <img align ='center' src='https://media2.giphy.com/media/UQDSBzfyiBKvgFcSTw/giphy.gif?cid=ecf05e47p3cd513axbek3f56ti3jzizq8hincw20jauyyfyw&rid=giphy.gif' width ='29' /> Here's some humor for you:
<img src="https://readme-jokes.vercel.app/api" alt="Error fetching resource, Refresh again to view Jokes Card" width = '11000' />
---

We'd love to hear from you! Connect with the S4101 – Group 16:

<p align="center">
  <a href="https://github.com/Kristov-Judilla"><img src="https://img.shields.io/badge/GitHub-Kristov--Judilla-181717?style=social&logo=github" alt="Kristov Judilla GitHub"></a>
  <!-- Add more team members' GitHub links here -->
</p>

<p align="center">
  <strong>Original Code</strong>: Made with ❤️ by MO-IT103 Team Daemon (Group 6)<br>
  <strong>Security Implementation</strong>: MO-IT153 – S4101 Group 16 (Ortega, A.M.; Estrella, F.N.; Echavez, V.G.; Sadio, J.)
</p>
