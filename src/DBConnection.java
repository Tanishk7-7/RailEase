import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static String host = System.getenv().getOrDefault("DB_HOST", "127.0.0.1");
    private static String port = System.getenv().getOrDefault("DB_PORT", "3306");
    private static String databaseName = System.getenv().getOrDefault("DB_NAME", "RailEaseDB");
    private static String username = System.getenv().getOrDefault("DB_USER", "root");
    private static String password = System.getenv("DB_PASSWORD");

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + databaseName
                + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    }

    public static Connection getConnection() throws SQLException {
        if (password == null || password.trim().isEmpty()) {
            password = System.getenv("DB_PASSWORD");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new SQLException("Database password not found. Please set the DB_PASSWORD environment variable or configure it in DB Settings.");
        }
        return DriverManager.getConnection(getJdbcUrl(), username, password);
    }

    public static boolean testConnection() {
        if (password == null || password.trim().isEmpty()) {
            password = System.getenv("DB_PASSWORD");
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String user) {
        username = user;
    }

    public static String getPassword() {
        if (password == null || password.trim().isEmpty()) {
            password = System.getenv("DB_PASSWORD");
        }
        return password != null ? password : "";
    }

    public static void setPassword(String pass) {
        password = pass;
    }

    public static String getDatabaseName() {
        return databaseName;
    }

    public static void setDatabaseName(String dbName) {
        databaseName = dbName;
    }
}
