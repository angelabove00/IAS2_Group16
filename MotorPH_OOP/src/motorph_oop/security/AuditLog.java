package motorph_oop.security;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import motorph_oop.util.Session;

// Records denied access attempts to audit.log (never logs passwords).
public final class AuditLog {

    private AuditLog() {}

    // audit.log sits in the app folder, or in motorph.dataDir when that is set (used by tests)
    private static String logFile() {

        String dir = System.getProperty("motorph.dataDir");

        return dir == null
                ? "audit.log"
                : Paths.get(dir, "audit.log").toString();
    }

    public static void denied(String permission) {

        String line = LocalDateTime.now()
                + " | DENIED"
                + " | empNo=" + Session.empNo
                + " | role=" + Session.role
                + " | permission=" + permission;

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(logFile()), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            writer.write(line);
            writer.newLine();

        } catch (IOException e) {
            System.err.println("[AuditLog] Could not write log: " + e.getMessage());
        }
    }
}