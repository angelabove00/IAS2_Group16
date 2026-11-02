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
| Password migration and backup | [`PasswordMigrationTool.java`](../MotorPH_OOP/src/motorph_oop/util/PasswordMigrationTool.java) | Control 1 migration check and rollback procedure below |
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

## 1. Password migration

### What is migrated

`PasswordMigrationTool` reads
`MotorPH_OOP/src/motorph_oop/resources/MotorPH_EmployeeLogin.csv`, leaves the
header unchanged, and replaces only values in the password column that are not
recognised bcrypt hashes. Hashing uses jBCrypt 0.4 with cost factor 12. Rows
already matching the bcrypt format are skipped, so a second run is intended to
be idempotent.

The application verifies passwords with `PasswordHasher.verify()`; it never
compares a supplied password directly with the stored value. `LoginDAO` also
rejects an attempt to write a non-bcrypt password.

**Status: implemented and demonstrated.** The tool creates a backup, skips
recognised bcrypt values, hashes legacy values, and reports migrated/skipped
rows. It does not provide an atomic transaction or automatic rollback.

### Normal migration procedure

1. Stop the application and make sure no other process is editing the login CSV.
2. Make a separate copy of the login CSV and restrict access to the migration
   operator. The tool itself creates
   `MotorPH_OOP/backup/MotorPH_EmployeeLogin.plaintext.bak.csv` before reading
   the source file; this backup contains the legacy plaintext values and must be
   treated as sensitive.
3. Run the migration tool from `MotorPH_OOP` using the project’s normal
   NetBeans/Ant run configuration.
4. Confirm the output reports the expected number of migrated rows and any
   skipped bcrypt rows. A malformed row is reported and skipped, not silently
   converted.
5. Confirm every data row has a bcrypt value and that the header and row count
   are unchanged. The recorded migration check is
   `rows=33 old-password-verifies=33 mismatches=0 header-same=true`.
6. Run `PasswordHashingTest` and perform a controlled login using a test
   account. Do not print or paste real passwords or the plaintext backup into
   tickets, logs, or source control.
7. Retain the backup only for the approved recovery period, then securely
   delete it according to the project’s data-retention policy.

### Error handling during migration

| Condition | Current behavior | Operator action |
|---|---|---|
| Login CSV is missing | Prints an error and exits before creating a backup | Restore the file from the approved backup, then stop and investigate the path/configuration |
| Login CSV is empty | Creates the backup, prints an error, and exits | Do not use the empty file; restore the backup and verify the source |
| Row has fewer than six columns | Reports the row number and the full row content, then skips that row | Treat console output as sensitive because the full row may contain a password; stop the release, repair the source row from an authoritative copy, and rerun validation |
| Password already has a valid bcrypt shape | Skips the row | Confirm that the hash belongs to the intended account and continue |
| Hashing, reading, or writing fails | The exception/error is surfaced to the console; the write is not reported as successful | Preserve the error output, do not delete the backup, restore the last known-good CSV, and validate before retrying |
| A hash is malformed or damaged | `verify()` returns `false`; it does not accept plaintext or throw for a malformed stored hash | Treat the account as unable to authenticate, reset it through an authorised account-management flow, and investigate the source file |

The migration writes the complete in-memory row list back to the original file.
It is not an atomic temp-file-and-rename transaction, and it does not perform a
post-write automatic rollback. Therefore the backup and the validation steps
above are mandatory. The rollback steps in this runbook are operator recovery
instructions, not an automatic feature of `PasswordMigrationTool`.

### Rollback and recovery if migration fails

1. Stop the application and prevent further writes to the login CSV.
2. Preserve the console output, timestamps, and the failed CSV as evidence.
   Do not overwrite the backup with a second migration attempt.
3. Verify that the backup is the expected file and is readable only by the
   operator. Never upload it to an issue or commit it.
4. Restore the backup to the application resource path:

   **PowerShell**
   ```powershell
   Copy-Item `
     .\backup\MotorPH_EmployeeLogin.plaintext.bak.csv `
     .\src\motorph_oop\resources\MotorPH_EmployeeLogin.csv `
     -Force
   ```

   **cmd.exe**
   ```text
   copy /Y backup\MotorPH_EmployeeLogin.plaintext.bak.csv src\motorph_oop\resources\MotorPH_EmployeeLogin.csv
   ```

5. If the application has already been changed to require bcrypt, do not leave
   the application running against the restored plaintext file. Either restore
   the matching pre-migration application build or rerun the migration after
   repairing the source.
6. Recount rows, verify the header, and run the password tests. For a damaged
   or partially migrated file, restore from the last known-good copy first
   rather than trying to repair individual hashes manually.
7. After recovery, rotate any credentials exposed in the plaintext backup and
   record the incident without recording the credentials themselves.

**Status: implemented but not fully tested.** Concurrent migration is not
supported. The stress evidence recorded eight
simultaneous first-login changes with six lost updates, although all resulting
rows remained syntactically valid bcrypt rows. Run migration and password
updates in a single-writer maintenance window.

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
