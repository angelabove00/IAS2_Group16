package motorph_oop.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Migration verification tool.
 * Checks that old plaintext passwords verify against their new bcrypt hashes.
 * This tool does not modify any files; it only reads and verifies.
 */
public class MigrationCheckTool {

    public static void main(String[] args) throws IOException {
        String backupPath = "backup/MotorPH_EmployeeLogin.plaintext.bak.csv";
        String csvPath = Constants.LOGIN_CSV;

        File backupFile = new File(backupPath);
        File csvFile = new File(csvPath);

        if (!backupFile.exists()) {
            System.err.println("Backup file not found: " + backupPath);
            System.err.println("Run PasswordMigrationTool first to create the backup.");
            System.exit(1);
        }

        if (!csvFile.exists()) {
            System.err.println("CSV file not found: " + csvPath);
            System.exit(1);
        }

        // Read backup (plaintext passwords)
        List<String[]> backupRows = readCSV(backupPath);
        // Read current CSV (bcrypt hashes)
        List<String[]> csvRows = readCSV(csvPath);

        if (backupRows.size() != csvRows.size()) {
            System.err.println("Row count mismatch: backup=" + backupRows.size() + " csv=" + csvRows.size());
            System.exit(1);
        }

        // Check header
        String backupHeader = backupRows.get(0)[0];
        String csvHeader = csvRows.get(0)[0];
        boolean headerSame = backupHeader.equals(csvHeader);

        int totalRows = backupRows.size() - 1; // exclude header
        int verified = 0;
        int mismatches = 0;

        // Verify each password
        for (int i = 1; i < backupRows.size(); i++) {
            String[] backupRow = backupRows.get(i);
            String[] csvRow = csvRows.get(i);

            if (backupRow.length < 6 || csvRow.length < 6) {
                System.err.println("Skipping malformed row " + i);
                continue;
            }

            String plaintext = backupRow[4];
            String hash = csvRow[4];

            if (PasswordHasher.verify(plaintext, hash)) {
                verified++;
            } else {
                mismatches++;
                System.err.println("Mismatch at row " + i + ": empNo=" + backupRow[0]);
            }
        }

        // Output result
        System.out.println("rows=" + totalRows + " old-password-verifies=" + verified + " mismatches=" + mismatches + " header-same=" + headerSame);

        if (mismatches > 0) {
            System.err.println("Migration verification failed: " + mismatches + " mismatches found");
            System.exit(1);
        }
    }

    private static List<String[]> readCSV(String path) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(line.split(",", -1));
            }
        }
        return rows;
    }
}
