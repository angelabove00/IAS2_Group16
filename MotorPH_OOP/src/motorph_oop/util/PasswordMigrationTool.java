package motorph_oop.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordMigrationTool {

    public static void main(String[] args) throws IOException {
        String csvPath = Constants.LOGIN_CSV;
        File csvFile = new File(csvPath);

        if (!csvFile.exists()) {
            System.err.println("CSV file not found: " + csvPath);
            System.exit(1);
        }

        // Create backup
        File backupDir = new File("backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        File backupFile = new File(backupDir, "MotorPH_EmployeeLogin.plaintext.bak.csv");
        Files.copy(csvFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Backup created: " + backupFile.getAbsolutePath());

        // Read CSV
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(csvFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (lines.isEmpty()) {
            System.err.println("CSV file is empty");
            System.exit(1);
        }

        String header = lines.get(0);
        int migrated = 0;
        int skipped = 0;

        // Process data rows
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(",", -1);
            if (parts.length < 6) {
                System.err.println("Skipping malformed row " + (i + 1) + ": " + line);
                continue;
            }

            String password = parts[4];
            if (PasswordHasher.isBcryptHash(password)) {
                skipped++;
                continue;
            }

            // Hash the password
            String hashed = PasswordHasher.hash(password);
            parts[4] = hashed;
            lines.set(i, String.join(",", parts));
            migrated++;
        }

        // Write back
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile.toPath(), StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }

        System.out.println("Migrated " + migrated + " of " + (lines.size() - 1) + " rows; skipped " + skipped + " already-hashed rows");
    }
}
