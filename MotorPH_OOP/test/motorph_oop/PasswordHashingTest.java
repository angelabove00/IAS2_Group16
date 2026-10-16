package motorph_oop;

import motorph_oop.dao.LoginDAO;
import motorph_oop.service.LoginService;
import motorph_oop.util.PasswordHasher;
import motorph_oop.util.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Arrays;

public class PasswordHashingTest {

    private static int passed = 0;
    private static int failed = 0;
    private static String testDataDir;

        public static void main(String[] args) throws Exception {
        // Set up temporary data directory
        setupTestDataDir();

        // RBAC (Control 2): the Control 1 tests run as an Admin so that
        // createUser / resetPassword / deleteUser are allowed
        motorph_oop.util.Session.role = "Admin";
        motorph_oop.util.Session.empNo = "10001";
      

        try {
            // Run all tests
            C1_T1();
            C1_T2();
            C1_T3();
            C1_T4();
            C1_T5();
            C1_T6();
            C1_T7();
            C1_T8();
            C1_T9();
            C1_T10();
            C1_T11();

            // Summary
            System.out.println("\n=== TEST SUMMARY ===");
            System.out.println("Passed: " + passed);
            System.out.println("Failed: " + failed);
            System.out.println("Total: " + (passed + failed));

            if (failed > 0) {
                System.exit(1);
            }
        } finally {
            // Clean up
            cleanupTestDataDir();
        }
    }

    private static void setupTestDataDir() throws IOException {
        testDataDir = Files.createTempDirectory("motorph-test-").toString();
        System.setProperty("motorph.dataDir", testDataDir);

        // Copy resources
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

    private static void C1_T1() {
        test("C1-T1", "authenticate with correct password", () -> {
            LoginService service = new LoginService();
            // First set a known password
            service.resetPassword("MGarcia", "Test@1234");

            var result = service.authenticate("MGarcia", "Test@1234");
            boolean pass = result != null && result.getEmpNo().equals("10001");

            // Reset back to temporary default (will be hashed)
            service.resetPassword("MGarcia", LoginService.TEMP_PASSWORD);
            return pass;
        });
    }

    private static void C1_T2() {
        test("C1-T2", "authenticate with wrong password", () -> {
            LoginService service = new LoginService();
            var result = service.authenticate("MGarcia", "WrongPassword123!");
            return result == null;
        });
    }

    private static void C1_T3() throws IOException {
        test("C1-T3", "all data rows are bcrypt hashes", () -> {
            String csvPath = Constants.LOGIN_CSV;
            int totalRows = 0;
            int hashedRows = 0;

            try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvPath), StandardCharsets.UTF_8)) {
                String line;
                reader.readLine(); // Skip header
                while ((line = reader.readLine()) != null) {
                    totalRows++;
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 5) {
                        String password = parts[4];
                        if (PasswordHasher.isBcryptHash(password)) {
                            hashedRows++;
                        }
                    }
                }
            }

            return totalRows == 33 && hashedRows == 33;
        });
    }

    private static void C1_T4() {
        test("C1-T4", "hash twice produces different strings", () -> {
            String hash1 = PasswordHasher.hash("Test@123");
            String hash2 = PasswordHasher.hash("Test@123");

            boolean different = !hash1.equals(hash2);
            boolean bothVerify = PasswordHasher.verify("Test@123", hash1) &&
                               PasswordHasher.verify("Test@123", hash2);

            return different && bothVerify;
        });
    }

    private static void C1_T5() {
        test("C1-T5", "verify handles plaintext/malformed gracefully", () -> {
            boolean verify1234 = !PasswordHasher.verify("1234", "1234");
            boolean verifyABC = !PasswordHasher.verify("abc", "abc");
            boolean verifyEmpty = !PasswordHasher.verify("", "");
            boolean verifyNull = !PasswordHasher.verify("test", null);

            return verify1234 && verifyABC && verifyEmpty && verifyNull;
        });
    }

    private static void C1_T6() {
        test("C1-T6", "entering hash as password is rejected", () -> {
            String hash = PasswordHasher.hash("Test@123");
            boolean result = !PasswordHasher.verify(hash, hash);
            return result;
        });
    }

    private static void C1_T7() {
        test("C1-T7", "createUser and resetPassword store hashes", () -> {
            LoginService service = new LoginService();
            LoginDAO dao = new LoginDAO();

            // Create user
            boolean created = service.createUser("99999", "testuser", "Test", "User", "Employee");
            if (!created) {
                return false;
            }

            // Check password is hashed
            String[] user = dao.findUserByUsername("testuser");
            boolean isHashed = PasswordHasher.isBcryptHash(user[4]);
            boolean isDefault = service.isDefaultPassword("testuser");

            // Reset password
            boolean reset = service.resetPassword("testuser", "Abc@1234");
            user = dao.findUserByUsername("testuser");
            boolean stillHashed = PasswordHasher.isBcryptHash(user[4]);
            boolean notDefault = !service.isDefaultPassword("testuser");

            // Verify new password works
            boolean works = service.authenticate("testuser", "Abc@1234") != null;

            // Cleanup
            service.deleteUser("testuser");

            return isHashed && isDefault && reset && stillHashed && notDefault && works;
        });
    }

    private static void C1_T8() throws IOException {
        test("C1-T8", "LoginDAO refuses plaintext updatePassword", () -> {
            String csvPath = Constants.LOGIN_CSV;
            String beforeHash = sha256File(csvPath);

            LoginDAO dao = new LoginDAO();
            boolean result = dao.updatePassword("MGarcia", "plaintext");

            String afterHash = sha256File(csvPath);

            return !result && beforeHash.equals(afterHash);
        });
    }

    private static void C1_T9() {
        test("C1-T9", "changeOwnPassword for forced first-login", () -> {
            LoginService service = new LoginService();

            // First reset to default
            service.resetPassword("Alim", "1234");

            // Change own password
            boolean changed = service.changeOwnPassword("Alim", "1234", "Newpass1!");

            // Verify old password rejected
            boolean oldRejected = service.authenticate("Alim", "1234") == null;

            // Verify new password works
            boolean newWorks = service.authenticate("Alim", "Newpass1!") != null;

            // Verify not default anymore
            boolean notDefault = !service.isDefaultPassword("Alim");

            // Reset back
            service.resetPassword("Alim", "1234");

            return changed && oldRejected && newWorks && notDefault;
        });
    }

    private static void C1_T10() {
        test("C1-T10", "changeOwnPassword rejects wrong current or new=1234", () -> {
            LoginService service = new LoginService();

            // Set up
            service.resetPassword("Alim", "Test@1234");

            // Wrong current password
            boolean wrongCurrent = !service.changeOwnPassword("Alim", "WrongPass", "Newpass1!");

            // New password equals TEMP_PASSWORD
            boolean newIsTemp = !service.changeOwnPassword("Alim", "Test@1234", "1234");

            // Verify password unchanged
            boolean unchanged = service.authenticate("Alim", "Test@1234") != null;

            // Reset back
            service.resetPassword("Alim", "1234");

            return wrongCurrent && newIsTemp && unchanged;
        });
    }

    private static void C1_T11() {
        test("C1-T11", "hashing cost is 12", () -> {
            String hash = PasswordHasher.hash("Test@123");
            return hash.startsWith("$2a$12$");
        });
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
        }
    }

    private static String sha256File(String path) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        byte[] hash = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @FunctionalInterface
    private interface TestRunnable {
        boolean run() throws Exception;
    }
}
