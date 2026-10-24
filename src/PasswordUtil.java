import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in environment", e);
        }
    }

    public static boolean verifyPassword(String enteredPassword, String storedHash) {
        if (enteredPassword == null || storedHash == null) {
            return false;
        }
        String calculatedHash = hashPassword(enteredPassword);
        return calculatedHash != null && calculatedHash.equalsIgnoreCase(storedHash.trim());
    }
}
