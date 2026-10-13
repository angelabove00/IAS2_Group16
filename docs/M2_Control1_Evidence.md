# Milestone 2 – Control 1: Password Hashing Evidence

## Project Information

- **Project**: MotorPH Payroll Management System
- **Group**: S4101 – Group 16
- **Branch**: M2_Control1_PasswordHashing
- **Base Commit**: cf2a213 (Initial commit)
- **Date**: 2026-10-13

## Control Summary

- **Control Name**: Password Hashing and Secure Verification
- **Control Type**: Technical
- **Mapped Surface**:
  - Component: `LoginService` / `LoginDAO` / `LoginPanel` / `MotorPH_EmployeeLogin.csv`
  - Surface Type: Code surface (authentication methods) and data surface (stored credentials)
- **Security Goal**: Confidentiality of credentials; stolen CSV ≠ usable passwords

## Tool/Library Information

- **jBCrypt**: Version 0.4
- **jBCrypt SHA-1**: af7e61017f73abb18ac4e036954f9f28c6366c07 (verified)
- **Cost Factor**: 12
- **JDK Version**: Java 23 (javac.source=23)
- **SpotBugs**: NOT RUN – SpotBugs 4.8.6 not installed on this system
- **Semgrep Version**: 1.180.0

## Files Changed

| File | Key Change |
|------|------------|
| `MotorPH_OOP/lib/jbcrypt-0.4.jar` | Added jBCrypt 0.4 library |
| `MotorPH_OOP/nbproject/project.properties` | Added jbcrypt to classpath; made LGoodDatePicker path relative |
| `MotorPH_OOP/src/motorph_oop/util/PasswordHasher.java` | New utility class for bcrypt hashing |
| `MotorPH_OOP/src/motorph_oop/util/PasswordMigrationTool.java` | New migration tool for existing passwords |
| `MotorPH_OOP/src/motorph_oop/util/Constants.java` | Added motorph.dataDir system property override for tests |
| `MotorPH_OOP/src/motorph_oop/service/LoginService.java` | Replaced plaintext comparison with PasswordHasher.verify; added changeOwnPassword; replaced hardcoded "1234" with TEMP_PASSWORD constant |
| `MotorPH_OOP/src/motorph_oop/dao/LoginDAO.java` | Replaced FileReader/FileWriter with Files.newBufferedReader/Writer (UTF-8); added guard to refuse non-bcrypt password writes |
| `MotorPH_OOP/src/motorph_oop/ui/LoginPanel.java` | Fixed openResetDialog to use authenticated username and currentPassword; removed username input field |
| `MotorPH_OOP/src/motorph_oop/ui/LoginManagementPanel.java` | Replaced hardcoded "1234" with LoginService.TEMP_PASSWORD |
| `MotorPH_OOP/src/motorph_oop/resources/MotorPH_EmployeeLogin.csv` | All 33 passwords migrated to bcrypt hashes |
| `MotorPH_OOP/test/motorph_oop/PasswordHashingTest.java` | New test harness with 11 test cases |
| `.gitignore` | Added build/, dist/, backup/, nbproject/private/, *.class |

## Before vs After

### CSV Sample

**Before (passwords masked):**
```
EmployeeNo,Username,Last Name,First Name,Password,AccessLevel
10001,MGarcia,Garcia,Manuel III,MM***,Admin
10002,Alim,Lim,Antonio,12***,Admin
10003,Baquino,Aquino,Bianca Sofia,12***,Admin
10004,Ireyes,Reyes,Isabella,12***,Admin
10005,Ehernandez,Hernandez,Eduard,MM***,IT
```

**After (bcrypt hashes):**
```
EmployeeNo,Username,Last Name,First Name,Password,AccessLevel
10001,MGarcia,Garcia,Manuel III,$2a$12$Y0635i9rp5wcYvUZtk.5W.Cl3YQQyLT4q4FpQ1N0G8uyxd/IBkk9a,Admin
10002,Alim,Lim,Antonio,$2a$12$rZDv2hQO1IL/8J2VajPIW.9QPsTYM/VHBbQ0FLNqLB4MnaJomU.jO,Admin
10003,Baquino,Aquino,Bianca Sofia,$2a$12$gzl9FnR8MN0k88hfwM8/QOWrwd40GClxBNrQC2EpQou.eAYDPsSN.,Admin
10004,Ireyes,Reyes,Isabella,$2a$12$AWTeQg8B1T/Go2V1qDcIf.wC4HzsAd3LcccMsl7Cr2rPmE8BrhiIa,Admin
10005,Ehernandez,Hernandez,Eduard,$2a$12$Wj99t2.NVsvw1AYXYRqG8eLs.Z7K9ZPjyyDmoNIgf8kgicaMBcWsa,IT
```

### authenticate() Before

```java
12:    public Employee authenticate(String username, String password) {
13:
14:        String[] user = loginDAO.findUserByUsername(username);
15:        if (user == null) return null;
16:
17:        String storedPassword = user[4];
18:        if (!storedPassword.equals(password)) return null;
19:
20:        String empNo = user[0];
21:        String lastName = user[2];
22:        String firstName = user[3];
23:        String role = user[5];
24:
25:        Employee employee;
26:
27:        switch (role.toUpperCase()) {
28:            case "ADMIN":
29:                employee = new AdminAccess();
30:                break;
31:            case "IT":
32:                employee = new ITAccess();
33:                break;
34:            case "HR":
35:                employee = new HRAccess();
36:                break;
37:            case "FINANCE":
38:                employee = new FinanceAccess();
39:                break;
40:            default:
41:                employee = new EmployeeAccess();
42:        }
43:
44:        employee.setEmpNo(empNo);
45:        employee.setLastName(lastName);
46:        employee.setFirstName(firstName);
47:
48:        return employee;
49:    }
```

### authenticate() After

```java
14:    public Employee authenticate(String username, String password) {
15:
16:        String[] user = loginDAO.findUserByUsername(username);
17:        if (user == null) return null;
18:
19:        String storedPassword = user[4];
20:        if (!PasswordHasher.verify(password, storedPassword)) return null;
21:
22:        String empNo = user[0];
23:        String lastName = user[2];
24:        String firstName = user[3];
25:        String role = user[5];
26:
27:        Employee employee;
28:
29:        switch (role.toUpperCase()) {
30:            case "ADMIN":
31:                employee = new AdminAccess();
32:                break;
33:            case "IT":
34:                employee = new ITAccess();
35:                break;
36:            case "HR":
37:                employee = new HRAccess();
38:                break;
39:            case "FINANCE":
40:                employee = new FinanceAccess();
41:                break;
42:            default:
43:                employee = new EmployeeAccess();
44:        }
45:
46:        employee.setEmpNo(empNo);
47:        employee.setLastName(lastName);
48:        employee.setFirstName(firstName);
49:
50:        return employee;
51:    }
```

### openResetDialog() Before

```java
291:    private void openResetDialog() {
292:
293:        String username =
294:                JOptionPane.showInputDialog(this, "Enter your username:");
295:
296:        if (username == null || username.trim().isEmpty()) {
297:
298:            JOptionPane.showMessageDialog(
299:                    this,
300:                    "Username cannot be empty."
301:            );
302:            return;
303:        }
304:
305:        JPasswordField newPass = new JPasswordField(12);
306:        JPasswordField confirmPass = new JPasswordField(12);
307:
308:        JLabel strengthLabel = new JLabel(" ");
309:        strengthLabel.setFont(new Font("Arial", Font.BOLD, 12));
310:
311:        JCheckBox showBox = new JCheckBox("Show Password");
312:
313:        showBox.addActionListener(e -> {
314:            char echo = showBox.isSelected() ? (char)0 : '•';
315:            newPass.setEchoChar(echo);
316:            confirmPass.setEchoChar(echo);
317:        });
318:
319:        // password strength indicator
320:        newPass.getDocument().addDocumentListener(
321:                (SimpleDocumentListener) e -> {
322:
323:                    String strength =
324:                            loginService.getPasswordStrength(
325:                                    new String(newPass.getPassword())
326:                            );
327:
328:                    strengthLabel.setText("Strength: " + strength);
329:
330:                    strengthLabel.setForeground(
331:                            strength.equals("Strong")
332:                                    ? new Color(0,153,0)
333:                                    : strength.equals("Normal")
334:                                    ? Color.ORANGE
335:                                    : Color.RED
336:                    );
337:                }
338:        );
339:
340:        JPanel panel = new JPanel(new GridBagLayout());
341:        panel.setPreferredSize(new Dimension(340,170));
342:        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
343:
344:        GridBagConstraints gbc = new GridBagConstraints();
345:        gbc.insets = new Insets(5,5,5,5);
346:        gbc.anchor = GridBagConstraints.WEST;
347:
348:        gbc.gridx = 0; gbc.gridy = 0;
349:        panel.add(new JLabel("New Password:"), gbc);
350:
351:        gbc.gridx = 1;
352:        panel.add(newPass, gbc);
353:
354:        gbc.gridx = 0; gbc.gridy = 1;
355:        panel.add(new JLabel("Confirm Password:"), gbc);
356:
357:        gbc.gridx = 1;
358:        panel.add(confirmPass, gbc);
359:
360:        gbc.gridx = 1; gbc.gridy = 2;
361:        panel.add(showBox, gbc);
362:
363:        gbc.gridx = 0; gbc.gridy = 3;
364:        gbc.gridwidth = 2;
365:        panel.add(strengthLabel, gbc);
366:
367:        int option = JOptionPane.showConfirmDialog(
368:                this,
369:                panel,
370:                "Reset Password",
371:                JOptionPane.OK_CANCEL_OPTION,
372:                JOptionPane.PLAIN_MESSAGE
373:        );
374:
375:        if (option != JOptionPane.OK_OPTION) return;
376:
377:        String newPassword =
378:                new String(newPass.getPassword()).trim();
379:
380:        String confirmPassword =
381:                new String(confirmPass.getPassword()).trim();
382:
383:        if (!newPassword.equals(confirmPassword)) {
384:
385:            JOptionPane.showMessageDialog(
386:                    this,
387:                    "Passwords do not match."
388:            );
389:            return;
390:        }
391:
392:        if (!loginService.validatePasswordRules(newPassword)) {
393:
394:            JOptionPane.showMessageDialog(
395:                    this,
396:                    "Password must be max 12 chars, include 1 uppercase, 1 number, and 1 special character."
397:            );
398:            return;
399:        }
400:
401:        boolean success =
402:                loginService.resetPassword(username, newPassword);
403:
404:        JOptionPane.showMessageDialog(
405:                this,
406:                success
407:                        ? "Password successfully reset."
408:                        : "Reset failed. User not found."
409:        );
410:    }
```

### openResetDialog() After

```java
290:    private void openResetDialog(String username, String currentPassword) {
291:
292:        JPasswordField newPass = new JPasswordField(12);
293:        JPasswordField confirmPass = new JPasswordField(12);
294:
295:        JLabel strengthLabel = new JLabel(" ");
296:        strengthLabel.setFont(new Font("Arial", Font.BOLD, 12));
297:
298:        JCheckBox showBox = new JCheckBox("Show Password");
299:
300:        showBox.addActionListener(e -> {
301:            char echo = showBox.isSelected() ? (char)0 : '•';
302:            newPass.setEchoChar(echo);
303:            confirmPass.setEchoChar(echo);
304:        });
305:
306:        // password strength indicator
307:        newPass.getDocument().addDocumentListener(
308:                (SimpleDocumentListener) e -> {
309:
310:                    String strength =
311:                            loginService.getPasswordStrength(
312:                                    new String(newPass.getPassword())
313:                            );
314:
315:                    strengthLabel.setText("Strength: " + strength);
316:
317:                    strengthLabel.setForeground(
318:                            strength.equals("Strong")
319:                                    ? new Color(0,153,0)
320:                                    : strength.equals("Normal")
321:                                    ? Color.ORANGE
322:                                    : Color.RED
323:                    );
324:                }
325:        );
326:
327:        JPanel panel = new JPanel(new GridBagLayout());
328:        panel.setPreferredSize(new Dimension(340,170));
329:        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
330:
331:        GridBagConstraints gbc = new GridBagConstraints();
332:        gbc.insets = new Insets(5,5,5,5);
333:        gbc.anchor = GridBagConstraints.WEST;
334:
335:        gbc.gridx = 0; gbc.gridy = 0;
336:        panel.add(new JLabel("New Password:"), gbc);
337:
338:        gbc.gridx = 1;
339:        panel.add(newPass, gbc);
340:
341:        gbc.gridx = 0; gbc.gridy = 1;
342:        panel.add(new JLabel("Confirm Password:"), gbc);
343:
344:        gbc.gridx = 1;
345:        panel.add(confirmPass, gbc);
346:
347:        gbc.gridx = 1; gbc.gridy = 2;
348:        panel.add(showBox, gbc);
349:
350:        gbc.gridx = 0; gbc.gridy = 3;
351:        gbc.gridwidth = 2;
352:        panel.add(strengthLabel, gbc);
353:
354:        int option = JOptionPane.showConfirmDialog(
355:                this,
356:                panel,
357:                "Reset Password",
358:                JOptionPane.OK_CANCEL_OPTION,
359:                JOptionPane.PLAIN_MESSAGE
360:        );
361:
362:        if (option != JOptionPane.OK_OPTION) return;
363:
364:        String newPassword =
365:                new String(newPass.getPassword()).trim();
366:
367:        String confirmPassword =
368:                new String(confirmPass.getPassword()).trim();
369:
370:        if (!newPassword.equals(confirmPassword)) {
371:
372:            JOptionPane.showMessageDialog(
373:                    this,
374:                    "Passwords do not match."
375:            );
376:            return;
377:        }
378:
379:        if (!loginService.validatePasswordRules(newPassword)) {
380:
381:            JOptionPane.showMessageDialog(
382:                    this,
383:                    "Password must be max 12 chars, include 1 uppercase, 1 number, and 1 special character."
384:            );
385:            return;
386:        }
387:
388:        boolean success =
389:                loginService.changeOwnPassword(username, currentPassword, newPassword);
390:
391:        if (success) {
392:            JOptionPane.showMessageDialog(
393:                    this,
394:                    "Password changed. Please log in with your new password."
395:            );
396:            passField.setText("");
397:        } else {
398:            JOptionPane.showMessageDialog(
399:                    this,
400:                    "Password change failed. Choose a different password that meets the rules."
401:            );
402:        }
403:    }
```

## Test Results

```
Test data directory: C:\Users\asaor\AppData\Local\Temp\motorph-test-9084774748728498023
[PASS] C1-T1 authenticate with correct password
[PASS] C1-T2 authenticate with wrong password
[PASS] C1-T3 all data rows are bcrypt hashes
[PASS] C1-T4 hash twice produces different strings
[PASS] C1-T5 verify handles plaintext/malformed gracefully
[PASS] C1-T6 entering hash as password is rejected
[PASS] C1-T7 createUser and resetPassword store hashes
[LoginDAO] Refused to store a non-bcrypt password value
[PASS] C1-T8 LoginDAO refuses plaintext updatePassword
[PASS] C1-T9 changeOwnPassword for forced first-login
[PASS] C1-T10 changeOwnPassword rejects wrong current or new=1234
[PASS] C1-T11 hashing cost is 12

=== TEST SUMMARY ===
Passed: 11
Failed: 0
Total: 11
```

**Result**: 11/11 tests passed

## Tool Results

### SpotBugs

**Before**: NOT RUN – SpotBugs 4.8.6 not installed on this system
**After**: NOT RUN – SpotBugs 4.8.6 not installed on this system

### Semgrep

**Before** (hardcoded "1234" rule):
- Total findings: 4
- DM_DEFAULT_ENCODING in LoginDAO: Not applicable (used Semgrep rule for hardcoded passwords)
- Findings:
  - LoginService.java line 92: `"1234"` (createUser)
  - LoginService.java line 123: `"1234"` (isDefaultPassword)
  - LoginManagementPanel.java line 129: `"1234"` (resetPassword)
  - LoginManagementPanel.java line 198: `"1234"` (viewResetRequests)

**After** (hardcoded "1234" rule):
- Total findings: 4
- Explanation: All 4 findings are now at `LoginService.TEMP_PASSWORD` (line 13) or its usages (lines 97, 128, 147). This is the intended single location for the temporary password constant.

**After** (p/java rules):
- Total findings: 0

### Static Checks

- `grep -rn "\.equals(password)\|storedPassword.equals" MotorPH_OOP/src`: No results (✓)
- `grep -rn '"1234"' MotorPH_OOP/src`: Exactly 1 line at `LoginService.TEMP_PASSWORD` (✓)

## Issues Encountered and Resolutions

1. **Compilation errors with PasswordHasher multi-catch**: Initial code used `IllegalArgumentException | RuntimeException` which is invalid since IllegalArgumentException is a subclass of RuntimeException. Fixed by catching only `RuntimeException`.

2. **Missing imports in LoginDAO and LoginService**: After adding PasswordHasher usage, needed to add imports for `PasswordHasher`, `Files`, `Paths`, `StandardCharsets`, and `StandardOpenOption`.

3. **Build artifact cleanup**: The original master branch had build/ and dist/ directories committed. Used `git rm -r --cached` to stop tracking them and added to .gitignore.

## Residual Risks (Deferred)

- **R-003**: The shared temporary password `1234` (stored as `LoginService.TEMP_PASSWORD`) remains. This is deferred to the Secure Password Reset control in Milestone 3.
- **Plaintext passwords in Git history**: Plaintext passwords remain in Git history before the `m2-c1-baseline` tag. All accounts should be rotated after this change.
- **No minimum password length**: `validatePasswordRules` only enforces a 12-character maximum, not a minimum.
- **In-memory lockout only**: The 3-attempt lockout is stored in-memory and resets on application restart.
- **Unencrypted employee and payroll data**: The employee and payroll CSV files are not encrypted (out of scope for Control 1).

## Rollback Instructions

To rollback this control:

1. Revert each commit with `git revert` (or check out `m2-c1-baseline`):
   ```bash
   git checkout m2-c1-baseline
   ```

2. Restore the plaintext CSV from the local backup:
   ```bash
   cp MotorPH_OOP/backup/MotorPH_EmployeeLogin.plaintext.bak.csv MotorPH_OOP/src/motorph_oop/resources/MotorPH_EmployeeLogin.csv
   ```

3. Rebuild the project.

## Test Command

```bash
cd MotorPH_OOP
javac -encoding UTF-8 -d build/classes -cp "lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" $(find src test -name "*.java")
java -cp "build/classes;lib/jbcrypt-0.4.jar;lib/JFolderChooser-0.9-src.jar;lib/LGoodDatePicker-11.2.1.jar;lib/itextpdf-5.5.13.3.jar" motorph_oop.PasswordHashingTest
```
