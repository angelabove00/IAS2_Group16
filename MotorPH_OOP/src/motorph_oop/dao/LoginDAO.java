
package motorph_oop.dao;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import motorph_oop.util.Constants;
import motorph_oop.util.PasswordHasher;

// data access object for login operations
// manages user credentials stored in login csv
public class LoginDAO {

    // read all rows from login csv (excluding header)
    private List<String[]> readLoginFile() {

        List<String[]> rows = new ArrayList<>();
        File file = new File(Constants.LOGIN_CSV);

        if (!file.exists()) return rows;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(Constants.LOGIN_CSV), StandardCharsets.UTF_8)) {

            String line;
            boolean header = true;

            while ((line = reader.readLine()) != null) {

                if (header) {
                    header = false;
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (parts.length >= 6) {
                    rows.add(parts);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return rows;
    }

    // find user by username
    public String[] findUserByUsername(String username) {

        for (String[] parts : readLoginFile()) {

            if (parts[1].trim().equalsIgnoreCase(username.trim())) {
                return parts;
            }
        }

        return null;
    }

    // update password for a user
    public boolean updatePassword(String username, String newPassword) {

        // Guard against plaintext writes
        if (!PasswordHasher.isBcryptHash(newPassword)) {
            System.err.println("[LoginDAO] Refused to store a non-bcrypt password value");
            return false;
        }

        File file = new File(Constants.LOGIN_CSV);
        List<String> lines = new ArrayList<>();

        boolean header = true;
        boolean updated = false;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(Constants.LOGIN_CSV), StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (header) {
                    lines.add(line);
                    header = false;
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (parts.length >= 6 &&
                        parts[1].trim().equalsIgnoreCase(username.trim())) {

                    parts[4] = newPassword;
                    line = String.join(",", parts);
                    updated = true;
                }

                lines.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!updated) return false;

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constants.LOGIN_CSV), StandardCharsets.UTF_8)) {

            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    // get all users
    public List<String[]> getAllUsers() {
        return readLoginFile();
    }

    // create new user login
    public boolean createUser(
            String empNo,
            String username,
            String lastName,
            String firstName,
            String password,
            String role) {

        // Guard against plaintext writes
        if (!PasswordHasher.isBcryptHash(password)) {
            System.err.println("[LoginDAO] Refused to store a non-bcrypt password value");
            return false;
        }

        File file = new File(Constants.LOGIN_CSV);

        try (BufferedWriter writer =
                     Files.newBufferedWriter(Paths.get(Constants.LOGIN_CSV), StandardCharsets.UTF_8,
                             StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            writer.write(
                    empNo + "," +
                    username + "," +
                    lastName + "," +
                    firstName + "," +
                    password + "," +
                    role
            );

            writer.newLine();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // delete user by username
    public boolean deleteUser(String username) {

        File file = new File(Constants.LOGIN_CSV);
        List<String> lines = new ArrayList<>();

        boolean header = true;
        boolean deleted = false;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(Constants.LOGIN_CSV), StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (header) {
                    lines.add(line);
                    header = false;
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (parts.length >= 6 &&
                        parts[1].trim().equalsIgnoreCase(username)) {

                    deleted = true;
                    continue;
                }

                lines.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(Constants.LOGIN_CSV), StandardCharsets.UTF_8)) {

            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return deleted;
    }

    // check if username already exists
    public boolean usernameExists(String username) {

        for (String[] parts : readLoginFile()) {

            if (parts[1].trim().equalsIgnoreCase(username.trim())) {
                return true;
            }
        }

        return false;
    }

    // check if employee already has login
    public boolean employeeLoginExists(String empNo) {

        for (String[] parts : readLoginFile()) {

            if (parts[0].trim().equals(empNo.trim())) {
                return true;
            }
        }

        return false;
    }
}