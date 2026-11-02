# Milestone 2 – Stress Testing Evidence

## Overview

This document provides detailed evidence and interpretation of the 13 stress test scenarios executed against the MotorPH security controls. The tests were run using a custom headless Java harness (StressTest.java) that calls the real service and DAO classes directly on a temporary data directory.

**Test Harness**: [`StressTest.java`](../MotorPH_OOP/test/motorph_oop/StressTest.java)
**Test Output**: [`StressTest-output.txt`](../MotorPH_OOP/test/motorph_oop/StressTest-output.txt)

---

## Control #1: Password Hashing – 7 Scenarios

### C1-1: Baseline Performance Test

**Objective**: Verify that normal login operations work with bcrypt hashing at cost factor 12.

**Procedure**:
- Run 10 correct logins through `LoginService.authenticate`
- Measure the average time per verification
- Verify that the correct password is still accepted

**Results**:
- 10/10 successful
- Average: 218.40 ms per verification
- Correct password accepted after all attempts

**Interpretation**:
The 218.40 ms delay is the intended bcrypt cost. This is imperceptible to a desktop user but severely limits brute force attacks. The old plaintext design required no hashing at all.

---

### C1-2: Online Brute Force (Single Thread)

**Objective**: Measure the rate of online password guessing attacks against a single account.

**Procedure**:
- 500 rapid wrong-password attempts against one account
- Using a 10-word common-password dictionary
- Single-threaded execution
- Verify correct password still works after failures

**Results**:
- 500 attempts in 106,450 ms
- 4.70 attempts per second
- 0 successful guesses
- No lockout applied
- Correct password still accepted after all 500 failures
- 0 audit.log lines written (failed logins not logged)

**Interpretation**:
The bcrypt cost factor 12 successfully limits online brute force to 4.70 attempts per second. This makes a full online attack impractical. The lack of audit logging for failed logins is a gap (no lockout at service layer, no trace of attack).

---

### C1-3: Online Brute Force (Concurrent)

**Objective**: Test if concurrent attacks can bypass the bcrypt rate limit.

**Procedure**:
- Same 500 attempts spread over 10 threads
- Each thread runs 50 attempts
- Measure total time and success rate

**Results**:
- 500 attempts across 10 threads in 14,321 ms
- 34.91 attempts per second
- 0 successful guesses
- 0 exceptions

**Interpretation**:
Concurrency increases the rate from 4.70 to 34.91 attempts per second, but this is still far below what would be practical for a full brute force attack. The concurrency control works correctly with no errors.

---

### C1-4: Username Enumeration by Timing

**Objective**: Detect if an attacker can determine if a username exists by measuring response time.

**Procedure**:
- 50 failed attempts with an existing username (MGarcia)
- 50 failed attempts with a non-existing username (DefinitelyMissingUser)
- Measure average time per attempt for each
- Calculate the ratio

**Results**:
- Existing user: 206.10 ms average per attempt
- Non-existing user: 0.06 ms average per attempt
- Ratio: 3,435:1

**Interpretation**:
The 3,435:1 timing difference is a side-channel vulnerability. An attacker can tell if a username exists because bcrypt verification takes 206 ms (username exists) vs 0.06 ms (username not found, short-circuits before hashing). This is a known weakness that should be addressed by constant-time verification.

---

### C1-5: Offline Cracking of Stolen Credential File

**Objective**: Measure how fast an attacker could crack stolen bcrypt hashes compared to plaintext.

**Procedure**:
- Copy all 33 bcrypt hashes from MotorPH_EmployeeLogin.csv
- Attack with a top-10 common-password list
- Measure guesses per second and cracked accounts
- Compare against simulated plaintext design

**Results**:
- 87 guesses over 33 accounts in 17,909 ms
- 4.86 guesses per second per core
- 27 of 33 accounts cracked
- All cracked accounts used the temporary default password "1234"
- All 33 rows were valid $2a$12$ bcrypt hashes
- Plaintext comparison: 150 million guesses per second

**Interpretation**:
Bcrypt reduced offline guessing from 150 million to 4.86 guesses per second (seven orders of magnitude). The 27 cracked accounts all used the weak default password "1234", which is residual risk R-003, not a bcrypt weakness. The DAO write guard held: no unhashed values reached the CSV.

---

### C1-6: Malformed and Oversized Input

**Objective**: Test if the system handles malformed inputs safely without accepting them or crashing.

**Procedure**:
- 15 test cases through `LoginService.authenticate`:
  1. Null username
  2. Null password
  3. Empty username
  4. Empty password
  5. 1,000,000-character password
  6. SQL injection fragment: `' OR '1'='1`
  7. CSV injection fragment: `MGarcia,evil,last,first,secret,Admin`
  8. Newline in username: `MGarcia\nforged`
  9. Null byte in password
  10. Unicode password: `пароль`
  11. Format string: `%s%s%s`
  12. Path traversal: `..\secret`
  13. Case-insensitive username: `mgarcia`
  14. Stored hash as password
  15. Whitespace-padded username: ` MGarcia `

**Results**:
- 0 accepted
- 14 rejected
- 1 uncaught NullPointerException

**Exception Details**:
```
java.lang.NullPointerException
  at motorph_oop.dao.LoginDAO.findUserByUsername(LoginDAO.java:57)
  at motorph_oop.service.LoginService.authenticate(LoginService.java:20)
```
The exception occurs because `LoginDAO.findUserByUsername` calls `username.trim()` before checking if username is null.

**Interpretation**:
No injection fragment, Unicode, newline, null byte, or 1 MB password reached the hash check or the CSV. The case-insensitive and whitespace-padded cases were correctly rejected. The null username exception is a bug that should be guarded against (check for null before calling trim()).

---

### C1-7: Concurrent Forced First-Login Changes

**Objective**: Test if concurrent password changes work correctly or if there's a race condition.

**Procedure**:
- Find 8 accounts still using the temporary default password
- All 8 call `changeOwnPassword` at the same instant
- Measure how many report success vs how many actually persist

**Results**:
- 8 accounts tested
- 8 returned true (success)
- Only 2 later logins actually worked
- 6 lost updates
- 0 exceptions
- Row count stayed at 33
- All 33 rows remained valid bcrypt hashes

**Interpretation**:
This is a genuine concurrency bug. `LoginService.changeOwnPassword` is not synchronized and the CSV read-modify-write is not atomic. Concurrent changes silently succeed in memory and then overwrite each other. In a real rollout where a whole company changes passwords at once, users would be silently reverted to the old default and might think the system is broken. This is the highest-priority fix.

---

## Control #2: RBAC – 6 Scenarios

### C2-1: Baseline Authorization Test

**Objective**: Verify that normal role-to-permission operations work correctly.

**Procedure**:
- Test four expected role-to-permission pairs:
  - HR viewing all employees
  - Finance calculating payroll
  - IT listing users
  - Employee viewing their own attendance

**Results**:
- HR_view_all: true
- Finance_calculate_payroll: true
- IT_list_users: true
- Employee_own_attendance: true

**Interpretation**:
All expected role-to-permission pairs work correctly on first attempt. The authorization matrix is functioning as designed.

---

### C2-2: Privilege Escalation Sweep

**Objective**: Test if an Employee can bypass RBAC by calling service methods directly (UI bypass).

**Procedure**:
- Set Session to Employee with empNo=10020
- Call 10 protected service methods directly, 50 times each
- Bypass the UI entirely
- Measure allowed/denied counts and timing

**Results**:
- All 10 methods denied 50 of 50 times
- 0 allowed
- 0 other exceptions
- Average check time: 0.22 to 0.98 ms depending on method
- audit.log received exactly 500 DENIED lines
- SHA-256 of employee and login CSVs unchanged before and after

**Methods Tested**:
1. EmployeeService.getEmployees: 0/50 allowed
2. EmployeeService.getEmployee(10001): 0/50 allowed
3. EmployeeService.deleteEmployee(10001): 0/50 allowed
4. EmployeeService.updateEmployee: 0/50 allowed
5. PayrollService.calculatePayroll: 0/50 allowed
6. PayrollService.generatePayslip: 0/50 allowed
7. LeaveService.getAllLeaves: 0/50 allowed
8. LoginService.getAllUsers: 0/50 allowed
9. LoginService.resetPassword: 0/50 allowed
10. AttendanceService.getAttendance(10001): 0/50 allowed

**Sample Audit Log Entry**:
```
2026-10-20T19:03:58.563994700 | DENIED | empNo=10020 | role=Employee | permission=VIEW_ALL_EMPLOYEES
```

**Interpretation**:
Authorization is enforced at the service boundary, not only by hiding UI controls. Even with direct service calls, the RBAC layer blocked all 500 unauthorized attempts with zero allowed and zero file changes. The checks are fast enough to add no meaningful latency.

---

### C2-3: Concurrent Denial Flood

**Objective**: Test if RBAC handles concurrent denial requests correctly without log corruption.

**Procedure**:
- 20 threads
- 250 denied calls each (5,000 total)
- Measure rate, errors, and audit log integrity

**Results**:
- 5,000 denied calls in 900 ms
- 5,555 checks per second
- 0 errors
- 0 allowed
- audit.log: 5,000 lines
- 5,000 lines matched exact well-formed denial pattern
- 0 malformed or interleaved
- Final file size: 508,587 bytes

**Interpretation**:
The authorization matrix held under heavy concurrent load. 5,000 simultaneous denied calls produced 5,000 well-formed, non-interleaved audit.log lines with zero errors. The audit logging mechanism is thread-safe even under stress.

---

### C2-4: Horizontal Access (IDOR)

**Objective**: Test if an Employee can access other employees' records (Insecure Direct Object Reference).

**Procedure**:
- Set Session to Employee with empNo=10020
- Request attendance records for all 34 employee IDs (10001 to 10034)
- Measure allowed vs denied

**Results**:
- 34 requests
- 1 allowed (the user's own record, empNo=10020)
- 33 denied
- own_record_allowed: true

**Interpretation**:
IDOR is fully blocked. Employees can only access their own records. The self-access check (requireSelf) correctly distinguishes between own and other employee numbers.

---

### C2-5: Session and Role Tampering

**Objective**: Test if malformed or forged role values can gain unauthorized access.

**Procedure**:
- Set Session.role directly to 10 role values
- Set Session.empNo to a cross-account value
- Test if authorization is bypassed

**Results**:
- `admin`: allowed (Session.role.toUpperCase() is applied)
- `Admin`: allowed
- `ADMIN`: allowed
- ` Admin` (leading space): denied
- `ADMIN ` (trailing space): denied
- `Administrator`: denied
- `root`: denied
- `` (empty string): denied
- `HR;Admin`: denied
- `Admin\n` (newline): denied
- Employee empNo=10001 attendance(10001): allowed (Session.empNo tampering works)

**Interpretation**:
The case-insensitive normalization (toUpperCase()) means `admin`, `Admin`, and `ADMIN` are all treated as the same role, which is acceptable. Whitespace, newline, and injection attempts are correctly denied. However, Session.empNo is trusted for self-access without re-verification, which is a latent design risk if the session is ever rebuilt or shared.

---

### C2-6: Data-File Tampering

**Objective**: Test if editing the CSV role assignment directly grants unauthorized access.

**Procedure**:
- Edit MotorPH_EmployeeLogin.csv to change an account's AccessLevel to Admin
- Attempt to log in with that account
- Measure the returned access class

**Results**:
- `returned_access_class=AdminAccess`

**Interpretation**:
Editing the CSV role to Admin successfully grants full admin access. The application reads roles from a writable CSV file, so anyone with write access to that file can grant themselves ADMIN. This is the most serious remaining risk (ISO 27001 A.9.4 issue). The solution is to move role assignments to a protected file/database or set OS-level ACLs on the application folder.

---

## Summary of Findings

### What Worked:
✅ Bcrypt reduced offline guessing from 150 million to 4.86 guesses per second (seven orders of magnitude)
✅ Online brute force limited to 4.70-34.91 attempts per second
✅ All 33 passwords successfully migrated to bcrypt
✅ DAO write guard prevented unhashed password storage
✅ RBAC blocked all 5,500 direct and concurrent unauthorized calls (0 allowed)
✅ Audit logging handled 5,000 concurrent writes with 0 errors
✅ IDOR fully blocked (33/33 unauthorized requests denied)
✅ Malformed inputs correctly rejected (14/15)

### Residual Risks (Not Control Failures):
1. **Temporary default password**: 27 of 33 accounts still use "1234" (easily cracked)
2. **No backend lockout**: 3-attempt lockout exists only in LoginPanel (UI layer)
3. **Failed logins not logged**: No audit trail for brute force attempts
4. **Null username exception**: Uncaught NPE in LoginDAO.findUserByUsername
5. **Concurrency bug**: changeOwnPassword not thread-safe (6 lost updates in 8 concurrent changes)
6. **Username timing attack**: 3,435:1 timing ratio allows username enumeration
7. **Writable role source**: CSV file can be edited to grant ADMIN
8. **Session.empNo trusted**: Self-access checks don't re-verify against login record

### Priority Fixes:
1. **Highest**: Synchronize changeOwnPassword (concurrency bug)
2. **High**: Move role assignments to protected file/database
3. **High**: Force password rotation for 27 accounts on default password
4. **Medium**: Move 3-attempt lockout to LoginService (service layer)
5. **Medium**: Log failed logins to audit.log
6. **Low**: Guard findUserByUsername against null input
7. **Low**: Constant-time username verification (fix timing attack)
8. **Low**: Re-verify Session.empNo in requireSelf/requireSelfOr
