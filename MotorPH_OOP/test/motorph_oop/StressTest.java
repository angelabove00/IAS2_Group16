package motorph_oop;

import motorph_oop.service.LoginService;
import motorph_oop.util.Constants;
import motorph_oop.util.PasswordHasher;
import motorph_oop.util.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Adversarial worksheet harness for the password controls.
 *
 * It intentionally runs against a copied resource directory and never prints
 * passwords, hashes, or other credential material.
 */
public final class StressTest {
    private static final String USERNAME = "MGarcia";
    private static final String BASELINE_PASSWORD = "StressBase1!";
    private static final List<String> DICTIONARY = Arrays.asList(
            "1234", "password", "123456", "qwerty", "admin",
            "letmein", "welcome", "password1", "abc123", "iloveyou");
    private static final String[] INJECTION_INPUTS = {
            "' OR '1'='1", "MGarcia,evil,last,first,secret,Admin"
    };

    private static Path dataDir;

    public static void main(String[] args) throws Exception {
        setupTestDataDir();
        try {
            Session.role = "Admin";
            Session.empNo = "stress-harness";
            new LoginService().resetPassword(USERNAME, BASELINE_PASSWORD);
            System.out.println("=== STRESS TEST EVIDENCE (credential values redacted) ===");
            baseline();
            onlineBruteForceSingleThread();
            onlineBruteForceConcurrent();
            usernameEnumeration();
            offlineCracking();
            malformedInput();
            concurrentFirstLoginChanges();
        } finally {
            deleteDirectory(dataDir);
        }
    }

    private static void setupTestDataDir() throws IOException {
        dataDir = Files.createTempDirectory("motorph-stress-");
        System.setProperty("motorph.dataDir", dataDir.toString());
        Path resources = Path.of("src", "motorph_oop", "resources");
        try (var files = Files.list(resources)) {
            files.forEach(source -> {
                try {
                    Files.copy(source, dataDir.resolve(source.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Could not copy test resource", e);
                }
            });
        }
        System.out.println("Data directory: temporary copy (path redacted)");
    }

    private static void baseline() {
        LoginService service = new LoginService();
        long start = System.nanoTime();
        int successful = 0;
        for (int i = 0; i < 10; i++) {
            if (service.authenticate(USERNAME, BASELINE_PASSWORD) != null) {
                successful++;
            }
        }
        long elapsed = elapsedMillis(start);
        boolean correctAccepted = service.authenticate(USERNAME, BASELINE_PASSWORD) != null;
        System.out.printf(Locale.ROOT,
                "C1-1 Baseline: attempts=10, successful=%d, average_ms=%.3f, "
                        + "correct_accepted=%s%n",
                successful, elapsed / 10.0, correctAccepted);
    }

    private static void onlineBruteForceSingleThread() {
        LoginService service = new LoginService();
        long start = System.nanoTime();
        int successes = 0;
        for (int i = 0; i < 500; i++) {
            String guess = DICTIONARY.get((i + 1) % DICTIONARY.size());
            if (service.authenticate(USERNAME, guess) != null) {
                successes++;
            }
        }
        long elapsed = elapsedMillis(start);
        boolean correctAccepted = service.authenticate(USERNAME, BASELINE_PASSWORD) != null;
        System.out.printf(Locale.ROOT,
                "C1-2 Online brute force (single): attempts=500, successful_guesses=%d, "
                        + "total_ms=%d, attempts_per_second=%.2f, correct_accepted_after_failures=%s, "
                        + "audit_lines=%d%n",
                successes, elapsed, rate(500, elapsed), correctAccepted, auditLineCount());
    }

    private static void onlineBruteForceConcurrent() throws Exception {
        LoginService service = new LoginService();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Callable<Boolean>> calls = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            final int index = i;
            calls.add(() -> {
                startGate.await();
                return service.authenticate(USERNAME,
                        DICTIONARY.get((index + 1) % DICTIONARY.size())) != null;
            });
        }
        long start = System.nanoTime();
        List<Future<Boolean>> results;
        try {
            startGate.countDown();
            results = pool.invokeAll(calls);
        } finally {
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.MINUTES);
        }
        int successes = 0;
        int exceptions = 0;
        for (Future<Boolean> result : results) {
            try {
                if (result.get()) {
                    successes++;
                }
            } catch (Exception e) {
                exceptions++;
                printException("C1-3 worker", e);
            }
        }
        long elapsed = elapsedMillis(start);
        System.out.printf(Locale.ROOT,
                "C1-3 Online brute force (10 threads): attempts=500, successful_guesses=%d, "
                        + "exceptions=%d, total_ms=%d, attempts_per_second=%.2f%n",
                successes, exceptions, elapsed, rate(500, elapsed));
    }

    private static void usernameEnumeration() {
        LoginService service = new LoginService();
        long existing = timeAttempts(service, USERNAME, 50);
        long missing = timeAttempts(service, "DefinitelyMissingUser", 50);
        double existingAverage = existing / 50.0;
        double missingAverage = missing / 50.0;
        System.out.printf(Locale.ROOT,
                "C1-4 Username timing: existing_average_ms=%.3f, nonexistent_average_ms=%.3f, "
                        + "ratio=%.3f%n",
                existingAverage, missingAverage,
                missingAverage == 0 ? 0.0 : existingAverage / missingAverage);
    }

    private static void offlineCracking() throws IOException {
        List<String[]> rows = readRows();
        long start = System.nanoTime();
        int guesses = 0;
        int cracked = 0;
        List<Integer> crackedDictionaryIndexes = new ArrayList<>();
        for (String[] row : rows) {
            boolean found = false;
            for (int i = 0; i < DICTIONARY.size(); i++) {
                guesses++;
                if (PasswordHasher.verify(DICTIONARY.get(i), row[4])) {
                    cracked++;
                    crackedDictionaryIndexes.add(i + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                crackedDictionaryIndexes.add(0);
            }
        }
        long bcryptMs = elapsedMillis(start);

        List<String> plaintextSlots = new ArrayList<>();
        for (String[] row : rows) {
            plaintextSlots.add("not-the-password");
        }
        start = System.nanoTime();
        int plaintextGuesses = 0;
        int plaintextMatches = 0;
        for (int repetition = 0; repetition < 10_000; repetition++) {
            for (String plaintext : plaintextSlots) {
                for (String candidate : DICTIONARY) {
                    plaintextGuesses++;
                    if (plaintext.equals(candidate)) {
                        plaintextMatches++;
                    }
                }
            }
        }
        long plaintextMs = elapsedMillis(start);
        System.out.printf(Locale.ROOT,
                "C1-5 Offline cracking: accounts=%d, bcrypt_guesses=%d, bcrypt_ms=%d, "
                        + "bcrypt_guesses_per_second=%.2f, cracked_accounts=%d, "
                        + "cracked_dictionary_indexes=%s, plaintext_guesses=%d, plaintext_ms=%d, "
                        + "plaintext_guesses_per_second=%.2f, plaintext_matches=%d%n",
                rows.size(), guesses, bcryptMs, rate(guesses, bcryptMs), cracked,
                crackedDictionaryIndexes, plaintextGuesses, plaintextMs,
                rate(plaintextGuesses, plaintextMs), plaintextMatches);
    }

    private static void malformedInput() throws IOException {
        LoginService service = new LoginService();
        String storedHash = readRows().get(0)[4];
        List<String> labels = Arrays.asList(
                "null-username", "null-password", "empty-username", "empty-password",
                "million-character-password", "sql-fragment", "csv-fragment",
                "newline-username", "null-byte-password", "unicode-password",
                "format-string-password", "path-password", "case-insensitive-username",
                "stored-hash-password", "whitespace-username");
        List<String> usernames = Arrays.asList(
                null, USERNAME, "", USERNAME, USERNAME, USERNAME, USERNAME,
                "MGarcia\nforged", USERNAME, USERNAME, USERNAME, USERNAME,
                "mgarcia", USERNAME, " " + USERNAME + " ");
        List<String> passwords = Arrays.asList(
                "redacted", null, "redacted", "", "x".repeat(1_000_000),
                INJECTION_INPUTS[0], INJECTION_INPUTS[1], "redacted", "\0redacted",
                "пароль", "%s%s%s", "..\\secret", storedHash, storedHash, "redacted");

        int accepted = 0;
        int rejected = 0;
        int exceptions = 0;
        for (int i = 0; i < labels.size(); i++) {
            try {
                boolean isAccepted = service.authenticate(usernames.get(i), passwords.get(i)) != null;
                if (isAccepted) {
                    accepted++;
                } else {
                    rejected++;
                }
            } catch (Throwable e) {
                exceptions++;
                System.out.println("C1-6 " + labels.get(i) + ": uncaught exception");
                printException("C1-6 " + labels.get(i), e);
            }
        }
        System.out.printf(Locale.ROOT,
                "C1-6 Malformed/oversized input: cases=%d, accepted=%d, rejected=%d, exceptions=%d%n",
                labels.size(), accepted, rejected, exceptions);
    }

    private static void concurrentFirstLoginChanges() throws Exception {
        LoginService service = new LoginService();
        List<String[]> rowsBefore = readRows();
        List<String> temporaryUsers = new ArrayList<>();
        for (String[] row : rowsBefore) {
            if (PasswordHasher.verify(LoginService.TEMP_PASSWORD, row[4])) {
                temporaryUsers.add(row[1]);
                if (temporaryUsers.size() == 8) {
                    break;
                }
            }
        }
        if (temporaryUsers.isEmpty()) {
            System.out.println("C1-7 Concurrent first-login changes: no temporary-password accounts; skipped");
            return;
        }

        ExecutorService pool = Executors.newFixedThreadPool(temporaryUsers.size());
        CountDownLatch gate = new CountDownLatch(1);
        List<Future<Boolean>> changes = new ArrayList<>();
        for (int i = 0; i < temporaryUsers.size(); i++) {
            final String username = temporaryUsers.get(i);
            final String newPassword = "StressNew" + i + "!";
            changes.add(pool.submit(() -> {
                gate.await();
                return service.changeOwnPassword(username, LoginService.TEMP_PASSWORD, newPassword);
            }));
        }
        gate.countDown();
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.MINUTES);

        int returnedTrue = 0;
        int exceptions = 0;
        for (Future<Boolean> change : changes) {
            try {
                if (change.get()) {
                    returnedTrue++;
                }
            } catch (Exception e) {
                exceptions++;
                printException("C1-7 worker", e);
            }
        }
        int laterLogins = 0;
        for (int i = 0; i < temporaryUsers.size(); i++) {
            if (service.authenticate(temporaryUsers.get(i), "StressNew" + i + "!") != null) {
                laterLogins++;
            }
        }
        List<String[]> rowsAfter = readRows();
        int validHashes = 0;
        for (String[] row : rowsAfter) {
            if (row.length >= 5 && PasswordHasher.isBcryptHash(row[4])) {
                validHashes++;
            }
        }
        int lostUpdates = returnedTrue - laterLogins;
        System.out.printf(Locale.ROOT,
                "C1-7 Concurrent first-login changes: accounts=%d, returned_true=%d, "
                        + "later_logins_working=%d, lost_updates=%d, exceptions=%d, "
                        + "row_count_before=%d, row_count_after=%d, valid_bcrypt_rows=%d/%d%n",
                temporaryUsers.size(), returnedTrue, laterLogins, lostUpdates, exceptions,
                rowsBefore.size(), rowsAfter.size(), validHashes, rowsAfter.size());
    }

    private static String findDictionaryPassword(LoginService service, String username) {
        for (String candidate : DICTIONARY) {
            if (service.authenticate(username, candidate) != null) {
                return candidate;
            }
        }
        return null;
    }

    private static long timeAttempts(LoginService service, String username, int count) {
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            service.authenticate(username, "not-the-password");
        }
        return elapsedMillis(start);
    }

    private static List<String[]> readRows() throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(Constants.LOGIN_CSV),
                StandardCharsets.UTF_8)) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    rows.add(parts);
                }
            }
        }
        return rows;
    }

    private static int auditLineCount() {
        Path audit = dataDir.resolve("audit.log");
        try {
            if (!Files.exists(audit)) {
                return 0;
            }
            try (var lines = Files.lines(audit, StandardCharsets.UTF_8)) {
                return (int) lines.count();
            }
        } catch (IOException e) {
            printException("audit-log-read", e);
            return -1;
        }
    }

    private static long elapsedMillis(long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }

    private static double rate(int count, long milliseconds) {
        return milliseconds == 0 ? Double.POSITIVE_INFINITY : count * 1000.0 / milliseconds;
    }

    private static void printException(String context, Throwable error) {
        System.out.println(context + ": " + error.getClass().getName());
        for (StackTraceElement element : error.getStackTrace()) {
            System.out.println("  at " + element);
        }
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            List<Path> reverse = paths.sorted(Collections.reverseOrder()).toList();
            for (Path path : reverse) {
                Files.deleteIfExists(path);
            }
        }
    }
}
