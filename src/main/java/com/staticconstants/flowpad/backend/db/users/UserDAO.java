// File: src/main/java/com/staticconstants/flowpad/backend/db/users/UserDAO.java
package com.staticconstants.flowpad.backend.db.users;

import com.staticconstants.flowpad.backend.db.DbHandler;
import com.staticconstants.flowpad.backend.security.PasswordHasher;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class UserDAO {

    public boolean checkLogin(String username, char[] password) throws Exception {
        PreparedStatement stmt = DbHandler.getInstance().getConnection().prepareStatement(
                "SELECT hashedPassword, encodedSalt FROM Users WHERE username = ?"
        );

        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String storedHashBase64 = rs.getString("hashedPassword");
            String storedSaltBase64 = rs.getString("encodedSalt");

            // Here's the actual check
            return PasswordHasher.verifyPassword(password, storedHashBase64, storedSaltBase64);
        }

        return false; // username not found
    }


    public CompletableFuture<Boolean> checkLoginAsync(String username, char[] password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return checkLogin(username, password);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }




    public CompletableFuture<Boolean> updatePassword(String username, char[] newPassword) {
        return DbHandler.getInstance().dbOperation(conn -> {
            var hashed = PasswordHasher.hashPassword(newPassword);
            String sql = "UPDATE Users SET hashedPassword = ?, encodedSalt = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, hashed.getHashedPasswordBase64());
                stmt.setString(2, hashed.getEncodedSaltBase64());
                stmt.setString(3, username);
                int affected = stmt.executeUpdate();
                return affected > 0;
            }
        });
    }

    public CompletableFuture<LoginResult> login(String username, char[] password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean valid = checkLogin(username, password);
                return valid ? LoginResult.SUCCESS : LoginResult.FAILURE;
            } catch (Exception e) {
                e.printStackTrace();
                return LoginResult.ERROR;
            }
        });
    }

    public CompletableFuture<Void> createTable() {
        return DbHandler.getInstance().dbOperation(conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS Users (
                    username TEXT PRIMARY KEY,
                    hashedPassword TEXT NOT NULL,
                    encodedSalt TEXT NOT NULL,
                    firstName TEXT NOT NULL,
                    lastName TEXT NOT NULL
                )
            """);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Boolean> insert(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var conn = DbHandler.getInstance().getConnection();
                var stmt = conn.prepareStatement("""
                INSERT INTO Users (username, hashedPassword, encodedSalt, firstName, lastName)
                VALUES (?, ?, ?, ?, ?)
            """);
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getHashedPassword().getHashedPasswordBase64());
                stmt.setString(3, user.getHashedPassword().getEncodedSaltBase64());
                stmt.setString(4, user.getFirstName());
                stmt.setString(5, user.getLastName());
                stmt.executeUpdate();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

}




