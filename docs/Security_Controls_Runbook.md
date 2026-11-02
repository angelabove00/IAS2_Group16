# Security Controls Runbook

This document explains the password migration and role-based access control (RBAC)
controls in the MotorPH application. It is an operational guide and test record:
the implementation evidence remains in [M2_Control1_Evidence.md](./M2_Control1_Evidence.md),
[M2_Control2_Evidence.md](./M2_Control2_Evidence.md), and the files under
[docs/evidence/](./evidence/).

This runbook describes the current repository as it exists; it is not evidence
that future recommendations have already been implemented. The labels below
make that distinction explicit:

- **Implemented and demonstrated** means the behavior exists in the current
  Java source and is covered by the recorded test output.
- **Implemented but not fully tested** means the behavior exists in source, but
  the listed production condition is not covered by the test harness.
- **Recommended improvement** means a hardening step that should be completed
  before production deployment; it must not be presented as a current control.

During the demonstration, open the referenced source file, run the relevant
test class, and compare the output with the expected result. The primary
implementation trace is:

| Control | Current implementation | Demonstration evidence |
|---|---|---|
| Bcrypt hashing and verification | [`PasswordHasher.java`](../MotorPH_OOP/src/motorph_oop/util/PasswordHasher.java), [`LoginService.java`](../MotorPH_OOP/src/motorph_oop/service/LoginService.java), [`LoginDAO.java`](../MotorPH_OOP/src/motorph_oop/dao/LoginDAO.java) | [`PasswordHashingTest.java`](../MotorPH_OOP/test/motorph_oop/PasswordHashingTest.java), [`test-output.txt`](./evidence/after/test-output.txt) |
| Password migration and backup | [`PasswordMigrationTool.java`](../MotorPH_OOP/src/motorph_oop/util/PasswordMigrationTool.java), [`MigrationCheckTool.java`](../MotorPH_OOP/src/motorph_oop/util/MigrationCheckTool.java) | [`migration-check.txt`](./evidence/after/migration-check.txt) and migration check/rollback procedure below |
| Central RBAC matrix | [`AccessControl.java`](../MotorPH_OOP/src/motorph_oop/security/AccessControl.java), [`Permission.java`](../MotorPH_OOP/src/motorph_oop/security/Permission.java) | [`RBACTest.java`](../MotorPH_OOP/test/motorph_oop/RBACTest.java), [`rbac-test-output.txt`](./evidence/after/rbac-test-output.txt) |
| Service-layer enforcement | [`EmployeeService.java`](../MotorPH_OOP/src/motorph_oop/service/EmployeeService.java), [`LoginService.java`](../MotorPH_OOP/src/motorph_oop/service/LoginService.java), [`LeaveService.java`](../MotorPH_OOP/src/motorph_oop/service/LeaveService.java), [`AttendanceService.java`](../MotorPH_OOP/src/motorph_oop/service/AttendanceService.java), and [`PayrollService.java`](../MotorPH_OOP/src/motorph_oop/service/PayrollService.java) | RBAC test output and [`StressTest-output.txt`](../MotorPH_OOP/test/motorph_oop/StressTest-output.txt) |
| Denial audit logging | [`AuditLog.java`](../MotorPH_OOP/src/motorph_oop/security/AuditLog.java) | C2-T11 and denial-flood evidence below |

The source links are the authority if this document and a generated evidence
file ever disagree.

## Feedback addressed

This runbook was expanded in response to the documentation feedback from Milestone 2. The following table shows where each requested improvement is addressed:

| Feedback requirement | Where it is addressed |
|---|---|
| Explain the password migration process clearly | [What is migrated](#what-is-migrated) and [Normal migration procedure](#normal-migration-procedure) |
| Document rollback, error handling, and recovery after migration failure | [Error handling during migration](#error-handling-during-migration) and [Rollback and recovery if migration fails](#rollback-and-recovery-if-migration-fails) |
| Explain RBAC enforcement across roles and permissions | [RBAC enforcement](#2-rbac-enforcement) and [Roles and permissions](#roles-and-permissions) |
| Show evidence that unauthorized users cannot access or modify restricted records | [Unauthorized access test evidence](#unauthorized-access-test-evidence) and [Role-based restrictions](#role-based-restrictions) |
| Include audit log examples and explain protection of sensitive information | [Audit logging and sensitive data protection](#3-audit-logging-and-sensitive-data-protection) |
| Provide password hashing test procedures and expected results | [Password hashing](#password-hashing) |
| Provide malformed-input and damaged-hash test procedures and expected results | [Malformed inputs and damaged hashes](#malformed-inputs-and-damaged-hashes) |
| Provide role-based restriction test procedures and expected results | [Role-based restrictions](#role-based-restrictions) |
| Document static-analysis findings | [Static analysis](#static-analysis) |
| Document remaining limitations and untested scenarios | [Remaining limitations and untested scenarios](#5-remaining-limitations-and-untested-scenarios) |

The test results and limitations are reported as observed. Recommended
hardening actions are intentionally labelled as recommendations and are not
presented as completed implementation work.

## 1. Password control and stress evidence

### Control goal

Control 1 replaces plain password storage and `String.equals()` checks with
salted bcrypt hashes. It uses jBCrypt 0.4 at cost 12.

The control covers:

- [`PasswordHasher.java`](../MotorPH_OOP/src/motorph_oop/util/PasswordHasher.java)
- [`LoginService.java`](../MotorPH_OOP/src/motorph_oop/service/LoginService.java)
- [`LoginDAO.java`](../MotorPH_OOP/src/motorph_oop/dao/LoginDAO.java)
- `MotorPH_EmployeeLogin.csv`

`LoginDAO` refuses to save a value that is not a valid bcrypt hash.
`LoginService` uses `PasswordHasher.verify()` for login checks. First-login
password changes also save a bcrypt hash.

**Status: implemented and tested.**

### Migration process

[`PasswordMigrationTool.java`](../MotorPH_OOP/src/motorph_oop/util/PasswordMigrationTool.java)
performs these steps:

1. Checks that the login CSV exists.
2. Creates `backup\MotorPH_EmployeeLogin.plaintext.bak.csv`.
3. Reads the CSV.
4. Keeps rows that already contain a bcrypt hash.
5. Hashes old password values with bcrypt cost 12.
6. Reports migrated, skipped, and malformed rows.
7. Writes the rows back to the login CSV.

The migration check recorded in the Control 1 evidence is available in
[`migration-check.txt`](./evidence/after/migration-check.txt):

```text
rows=33 old-password-verifies=33 mismatches=0 header-same=true
```

This shows that all 33 old passwords matched their new hashes and that the
header stayed the same. The check was performed by hashing each password from
the pre-migration backup and verifying it against the corresponding hash in the
migrated CSV.

**Note:** The backup file `backup/MotorPH_EmployeeLogin.plaintext.bak.csv` is not
committed to the repository because it contains plaintext passwords. The
migration-check.txt file records the verification result from the original
migration run. If you have a local backup (e.g., from running the migration tool
yourself), you can run MigrationCheckTool to verify it:

```text
cd MotorPH_OOP
javac -encoding UTF-8 -d build/classes -cp "lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" src/motorph_oop/util/MigrationCheckTool.java
java -cp "build/classes;lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" motorph_oop.util.MigrationCheckTool
```

The tool reads the backup file and the current login CSV, verifies each old
password against its new hash, and outputs the verification result in the same
format as the evidence file.

The backup contains old passwords. Protect it as secret data. Do not commit it,
send it by email, or paste it into a ticket.

### Migration error handling

| Case | Current result | Required response |
|---|---|---|
| Login CSV is missing | The tool prints an error and exits | Check the path and restore the approved backup |
| Login CSV is empty | The tool creates a backup, prints an error, and exits | Restore the last good file |
| Row has fewer than six fields | The tool prints the full row and skips it | Stop the migration; repair the row; run the checks again |
| Value is already a bcrypt hash | The tool skips it | Check that it belongs to the correct account |
| Read, hash, or write error | The error reaches the console | Keep the backup; restore the last good file; check before retry |
| Damaged hash | `verify()` returns `false` | Reset the account through an approved account flow |

The tool writes the full row list back to the original file. It does not use an
atomic temporary file and does not roll back by itself.

### Rollback and recovery

If migration fails:

1. Stop the application.
2. Stop all writes to the login CSV.
3. Keep the failed file and error output for review.
4. Do not overwrite the backup.
5. Restore the backup.

PowerShell:

```powershell
Copy-Item `
  .\backup\MotorPH_EmployeeLogin.plaintext.bak.csv `
  .\src\motorph_oop\resources\MotorPH_EmployeeLogin.csv `
  -Force
```

Command Prompt:

```text
copy /Y backup\MotorPH_EmployeeLogin.plaintext.bak.csv src\motorph_oop\resources\MotorPH_EmployeeLogin.csv
```

6. Do not run bcrypt-only code against restored plain passwords.
7. Check the header, row count, and hash format.
8. Run `PasswordHashingTest`.
9. Change any password exposed in the backup.

Rollback is an operator procedure. It is not an automatic feature.

### Stress test method

[`StressTest.java`](../MotorPH_OOP/test/motorph_oop/StressTest.java) runs seven
bounded cases against a temporary copy of the data files. It calls the real
service and DAO classes. It does not change the source CSV files. It does not
print passwords or hashes. It uses `System.nanoTime()` for timing, SHA-256 for
file checks, and `PasswordHasher.verify()` for hash checks.

Evidence: [`StressTest-output.txt`](../MotorPH_OOP/test/motorph_oop/StressTest-output.txt).

### Stress test results

| Case | Test | Result |
|---|---|---|
| C1-1 | Ten correct logins | 10/10 succeeded; average 218.40 ms |
| C1-2 | 500 wrong guesses, one thread | 0 guesses succeeded; 4.70 attempts/second |
| C1-3 | 500 wrong guesses, ten threads | 0 guesses succeeded; 0 exceptions |
| C1-4 | Username timing | Existing: 206.10 ms; missing: 0.06 ms; ratio: 3,435:1 |
| C1-5 | Offline attack on 33 bcrypt hashes | 87 bcrypt guesses; 27/33 accounts cracked |
| C1-6 | 15 malformed or large inputs | 0 accepted; 14 rejected; 1 uncaught exception |
| C1-7 | Eight concurrent first-login changes | 8 returned true; only 2 later logins worked; 6 updates were lost |

The 27 cracked accounts used the temporary default password. This shows a
weak-password problem, not a bcrypt failure. The same test against simulated
plain storage ran at about 150 million guesses per second. The bcrypt test ran
at 4.86 guesses per second.

### Findings from the stress test

1. The service has no backend lockout. The three-attempt lockout exists in the
   login panel only.
2. Failed logins do not create audit entries.
3. A null username causes an uncaught `NullPointerException` in
   `LoginDAO.findUserByUsername`.
4. `changeOwnPassword` is not safe for concurrent calls. Eight calls returned
   success, but six updates were lost.
5. Twenty-seven accounts still use the temporary default password.

These are open findings. They do not cancel the result that bcrypt hashing,
hash verification, and the DAO write guard work as tested.

### Next fixes

1. Lock the password read-modify-write operation.
2. Move login lockout into `LoginService`.
3. Log failed logins.
4. Reject a null username before the DAO call.
5. Require users to replace the temporary password.

## 2. RBAC enforcement

**Status: implemented and demonstrated.** Authorization is enforced at the
service boundary, not only by hiding UI
controls. `AccessControl` is the single permission matrix. Each protected
service operation calls `require`, `requireSelfOr`, or `requireSelf`; a failed
check writes a denial event and throws `AccessDeniedException`. The UI uses
`AccessControl.can()` only to show or enable controls and is not the security
boundary.

### Roles and permissions

| Role | View all employees | Add/modify/delete employees | Calculate salary | Generate payslip | Approve leave | Manage user accounts |
|---|---:|---:|---:|---:|---:|---:|
| Admin | Yes | Yes | Yes | Yes | Yes | Yes |
| HR | Yes | Yes | No | No | Yes | No |
| Finance | Yes | No | Yes | Yes | No | No |
| IT | Yes | No | No | No | No | Yes |
| Employee | No | No | No | No | No | No |

The complete eight-permission matrix is defined in
[M2_Control2_Evidence.md](./M2_Control2_Evidence.md). Service coverage is:

- `EmployeeService`: list, add, modify, and delete operations require the
  corresponding employee permission; `getEmployee` permits self or
  `VIEW_ALL_EMPLOYEES`.
- `LoginService`: account creation, reset, listing, deletion, and reset-request
  administration require `MANAGE_USER_ACCOUNTS`.
- `LeaveService`: employees can submit and view only their own requests;
  approval and all-leave access require the appropriate permission.
- `PayrollService`: calculation and payslip generation allow self or the
  corresponding payroll permission.
- `AttendanceService`: check-in/check-out are self-only; viewing allows self or
  `VIEW_ALL_EMPLOYEES`.

No session denies all permissions. Unknown, empty, whitespace-padded, newline-
containing, or combined roles are not normalised into a valid role and receive
no permissions. Logout calls `Session.clear()`, removing the authorization
context.

### Unauthorized access test evidence

Run `RBACTest` after compiling the source and tests. The recorded result is
**13/13 passed**:

- The permission matrix was checked for all five roles and eight permissions.
- Employee, IT, HR, and Finance negative cases were exercised against service
  methods that read or modify employee, leave, payroll, attendance, and account
  records.
- Horizontal access requested 34 employee records and allowed only the caller’s
  own record (`allowed=1`, `denied=33`).
- A denial flood made 5,000 calls from 20 threads: `denied=5000`,
  `allowed=0`, `errors=0`; all 5,000 audit lines were well formed and not
  interleaved.
- The stress evidence shows 50 repeated calls denied for each restricted
  operation, with no CSV changes.

Expected result for every unauthorized read or modification is:

1. No restricted record is returned or changed.
2. `AccessDeniedException` is raised.
3. One well-formed `DENIED` audit entry is appended.

## 3. Audit logging and sensitive data protection

Denied events are written as UTF-8 lines to `audit.log` in the application data
directory (or the directory selected by `motorph.dataDir` in tests). A sample
entry is:

```text
2026-10-20T19:03:58.563994700 | DENIED | empNo=10020 | role=Employee | permission=VIEW_ALL_EMPLOYEES
```

The log records timestamp, employee number, role, and permission only. It does
not record passwords, password hashes, request bodies, payroll values, or
plaintext employee details. The audit test confirms denied actions are logged
and allowed actions are not.

**Status: implemented but not fully tested.** This is protection against
accidental credential disclosure, not a complete log-security solution. Anyone
with filesystem access can read, delete, or
modify the current log, and employee numbers and roles are still
security-relevant metadata. Production deployment should use restrictive file
permissions, append-only or centralised log storage, controlled retention, and
monitoring for tampering.

## 4. Detailed test procedures and expected results

From `MotorPH_OOP`, compile the project and tests with the existing NetBeans/
Ant configuration, then run:

```text
motorph_oop.PasswordHashingTest
motorph_oop.RBACTest
```

### Password hashing

| Test | Procedure | Expected result |
|---|---|---|
| Correct password | Hash a known test password and authenticate with it | Authentication succeeds |
| Wrong password | Authenticate with a different password | Authentication returns no user |
| Stored data | Inspect all 33 login rows with `isBcryptHash` | 33/33 are bcrypt hashes |
| Salt uniqueness | Hash the same input twice | Hash strings differ and both verify |
| Hash-as-password | Submit the stored hash as the password | Rejected |
| Cost | Inspect the bcrypt cost marker | Cost is 12 |

Recorded result: **C1-T1 through C1-T11, 11/11 passed**.

### Malformed inputs and damaged hashes

Call `PasswordHasher.verify()` with null plaintext, null hash, empty strings,
plaintext, malformed bcrypt text, and a damaged bcrypt string. Call
`PasswordHasher.hash()` with null and empty input as well.

Expected results: `verify()` returns `false` without accepting the input;
`hash()` rejects null/empty input with `IllegalArgumentException`; no plaintext
is written by `LoginDAO`. The dedicated hashing tests cover malformed,
plaintext, empty, null-hash, and hash-as-password cases. Direct
`LoginService.authenticate(null, ...)` is a known gap: the stress test recorded
an uncaught `NullPointerException` in `LoginDAO.findUserByUsername`.

### Role-based restrictions

For each role, set a controlled test session, invoke every service operation
listed in the RBAC section, and compare the result with the matrix. Repeat
employee operations using both the caller’s own employee number and another
employee number. Then call `Session.clear()` and repeat one protected action.

Expected results: only matrix-approved operations and explicitly permitted
self-access succeed; other operations throw `AccessDeniedException`, leave the
CSV files unchanged, and append one denial entry. After logout all protected
operations are denied. These expectations are evidenced by `RBACTest`,
`rbac-test-output.txt`, and `StressTest-output.txt`.

### Static analysis

Run the project’s recorded Semgrep and SpotBugs checks and retain the output
under [docs/evidence/after/](./evidence/after/).

Expected results and interpretation:

- Semgrep with the 60 Java rules: **0 findings**.
- The narrow legacy rule for hardcoded `"1234"` still reports **4 findings**,
  all at the intentional `LoginService.TEMP_PASSWORD` constant or its uses.
  This is a documented residual risk, not a clean security result.
- SpotBugs after Control 1: 85 warnings, including default-encoding warnings
  in non-login CSV/payroll code and unchecked directory creation in the
  migration tool. These are not silently treated as resolved.

**Status: implemented checks with findings.** “0 Semgrep findings” applies to
the recorded 60-rule Java scan only. It does not mean that all security risks
are absent. The hardcoded temporary password and SpotBugs warnings remain
documented findings.

## 5. Remaining limitations and untested scenarios

The following items are intentionally not described as completed controls:

- The shared temporary password `1234` remains until a secure reset control is
  implemented; credential rotation is required for real deployment.
- The null-username authentication path throws instead of returning a normal
  authentication failure.
- Existing stress evidence shows a large username timing difference
  (`existing_average_ms=206.100` versus `nonexistent_average_ms=0.060`).
- The migration tool has no atomic write, automatic rollback, or concurrency
  lock.
- Static `Session` fields are not safe for concurrent users in one JVM.
- CSV files and the audit log are not encrypted; filesystem access bypasses
  application RBAC.
- The audit log is local and tamperable without central collection.
- A malformed migration row is currently printed in full to stderr; a
  malformed row could therefore expose a legacy password to console history.
- The tests do not provide a production multi-process migration test, crash or
  power-loss recovery test, filesystem-permission test, backup confidentiality
  test, log rotation/retention test, or a full UI end-to-end authorization test.
- The tests do not establish resistance to denial-of-service, password
  spraying, or network interception because this is a desktop CSV application.

## 6. Demonstration checklist

1. Open `PasswordMigrationTool.java` and show the backup creation, bcrypt
   detection, malformed-row handling, and write-back logic.
2. Open `MigrationCheckTool.java` and show that it reads the backup and current
   CSV, verifies each old password against its hash, and outputs the check result.
   Note that the backup is not in the repository because it contains plaintext
   passwords.
3. Show the `migration-check.txt` evidence file and explain that all 33 passwords
   verified successfully when the migration was originally run.
2. Show `PasswordHasher.java` and `LoginDAO.java`, then run
   `PasswordHashingTest`; expected result is 11/11 passed.
3. Open `AccessControl.java` and `Permission.java`, then map each role to the
   permission table in this document.
4. Open one protected service method and show its authorization call before the
   DAO operation.
5. Run `RBACTest`; expected result is 13/13 passed. Show that denied calls
   throw `AccessDeniedException` and do not change the CSV files.
6. Open `AuditLog.java` and show that the denial entry contains only timestamp,
   employee number, role, and permission—not passwords or hashes.
7. Demonstrate the limitations honestly: the null-username exception,
   temporary password, local/tamperable logs, non-atomic migration, and
   remaining static-analysis findings are open issues, not passing controls.
