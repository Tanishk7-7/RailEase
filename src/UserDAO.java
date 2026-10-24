import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final UserAccount account;

        public AuthResult(boolean success, String message, UserAccount account) {
            this.success = success;
            this.message = message;
            this.account = account;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public UserAccount getAccount() {
            return account;
        }
    }

    public AuthResult authenticate(String username, String password, String expectedRole) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Username and password cannot be empty.", null);
        }

        String cleanUsername = username.trim();
        String cleanRole = expectedRole != null ? expectedRole.trim().toUpperCase() : "PASSENGER";

        String sql = "SELECT u.user_id, u.passenger_id, u.username, u.password_hash, u.role, p.passenger_name, p.phone "
                + "FROM UserAccount u "
                + "LEFT JOIN Passenger p ON u.passenger_id = p.passenger_id "
                + "WHERE u.username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cleanUsername);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return new AuthResult(false, "No user account found matching '" + cleanUsername + "'.", null);
                }

                String storedRole = rs.getString("role");
                if (storedRole != null) {
                    storedRole = storedRole.trim().toUpperCase();
                }

                // Verify requested role matches stored role
                if (!cleanRole.equalsIgnoreCase(storedRole)) {
                    if ("ADMIN".equalsIgnoreCase(storedRole)) {
                        return new AuthResult(false, "Access Denied: '" + cleanUsername + "' is an Administrator account. Please use Admin Login.", null);
                    } else {
                        return new AuthResult(false, "Access Denied: '" + cleanUsername + "' is a Passenger account. Please use Passenger Login.", null);
                    }
                }

                String storedHash = rs.getString("password_hash");

                // Check for uninitialized placeholder hashes
                if (storedHash != null && storedHash.startsWith("DEMO_HASH_")) {
                    return new AuthResult(false, "Account credentials are uninitialized demonstration placeholders. Please initialize demo credentials first.", null);
                }

                // Verify password securely using SHA-256
                if (!PasswordUtil.verifyPassword(password, storedHash)) {
                    return new AuthResult(false, "Incorrect password. Please verify and try again.", null);
                }

                int userId = rs.getInt("user_id");
                Integer passengerId = (Integer) rs.getObject("passenger_id");
                String passengerName = rs.getString("passenger_name");
                String phone = rs.getString("phone");

                UserAccount userAccount = new UserAccount(userId, passengerId, cleanUsername, storedRole, passengerName, phone);
                return new AuthResult(true, "Authentication successful.", userAccount);
            }
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            return new AuthResult(false, "Database connection error during login. Please check MySQL settings.", null);
        }
    }

    public List<Passenger> getAllPassengers() {
        List<Passenger> list = new ArrayList<>();
        String sql = "SELECT passenger_id, passenger_name, age, gender, phone FROM Passenger ORDER BY passenger_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Passenger(
                        rs.getInt("passenger_id"),
                        rs.getString("passenger_name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all passengers: " + e.getMessage());
        }
        return list;
    }

    public int initializeDemoPasswordsIfPlaceholders() {
        String countSql = "SELECT COUNT(*) FROM UserAccount WHERE password_hash LIKE 'DEMO_HASH_%'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {

            if (rs.next() && rs.getInt(1) > 0) {
                String selectSql = "SELECT user_id, username FROM UserAccount WHERE password_hash LIKE 'DEMO_HASH_%'";
                String updateSql = "UPDATE UserAccount SET password_hash = ? WHERE user_id = ?";

                int updatedCount = 0;
                try (Statement selStmt = conn.createStatement();
                     ResultSet userRs = selStmt.executeQuery(selectSql);
                     PreparedStatement updStmt = conn.prepareStatement(updateSql)) {

                    while (userRs.next()) {
                        int userId = userRs.getInt("user_id");
                        String username = userRs.getString("username");

                        // Generate deterministic demo password based on username prefix:
                        // e.g. "admin_demo" -> "admin123", "aarav_demo" -> "aarav123"
                        String demoPassword;
                        if (username != null && username.contains("_")) {
                            demoPassword = username.substring(0, username.indexOf("_")) + "123";
                        } else if (username != null) {
                            demoPassword = username + "123";
                        } else {
                            demoPassword = "password123";
                        }

                        String hashed = PasswordUtil.hashPassword(demoPassword);
                        updStmt.setString(1, hashed);
                        updStmt.setInt(2, userId);
                        updStmt.executeUpdate();
                        updatedCount++;
                    }
                }
                System.out.println("Initialized " + updatedCount + " demo account password hashes in UserAccount with SHA-256.");
                return updatedCount;
            }
        } catch (SQLException e) {
            System.err.println("Notice: Could not check or initialize demo passwords: " + e.getMessage());
        }
        return 0;
    }
}
