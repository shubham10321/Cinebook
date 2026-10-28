package cinema.dao;

import java.sql.*;

import cinema.model.User;
import cinema.util.DBConnection;
import cinema.util.OtpUtil;
import cinema.util.PasswordUtil;

public class UserDAO {

    // ─── Login ────────────────────────────────────────────────────────────────

    public User login(String username, String password) throws Exception {
        String sql = "SELECT user_id, name, username, password_hash, email, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!PasswordUtil.matches(password, rs.getString("password_hash"))) return null;
                return map(rs);
            }
        }
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    public boolean usernameExists(String username) throws Exception {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void register(String name, String username, String password, String email) throws Exception {
        String sql = "INSERT INTO users (name, username, password_hash, email, role) VALUES (?, ?, ?, ?, 'CUSTOMER')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, email == null ? "" : email);
            ps.executeUpdate();
        }
    }

    /** Backward-compatible register without email. */
    public void register(String name, String username, String password) throws Exception {
        register(name, username, password, "");
    }

    // ─── OTP / Forgot Password ────────────────────────────────────────────────

    /**
     * Looks up the user by username or email, generates an OTP,
     * saves it to DB (expires in 10 minutes), and returns the OTP
     * string so the UI can display it (demo mode).
     *
     * @return OTP string, or null if user not found
     */
    public String generateAndSaveOtp(String usernameOrEmail) throws Exception {
        String sql = "SELECT user_id, email FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                int userId = rs.getInt("user_id");
                String email = rs.getString("email");
                String otp = OtpUtil.generate();
                saveOtp(userId, otp);
                OtpUtil.send(email == null ? usernameOrEmail : email, otp);
                return otp;
            }
        }
    }

    private void saveOtp(int userId, String otp) throws Exception {
        String sql = "UPDATE users SET otp_code = ?, otp_expiry = DATE_ADD(NOW(), INTERVAL 10 MINUTE) WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Verifies the OTP for the given username/email.
     * Returns true if OTP matches and is not expired.
     */
    public boolean verifyOtp(String usernameOrEmail, String otp) throws Exception {
        String sql = "SELECT 1 FROM users WHERE (username = ? OR email = ?) "
                   + "AND otp_code = ? AND otp_expiry > NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);
            ps.setString(3, otp);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    /** Reset the password after OTP verification. Clears the OTP fields. */
    public void resetPassword(String usernameOrEmail, String newPassword) throws Exception {
        String sql = "UPDATE users SET password_hash = ?, otp_code = NULL, otp_expiry = NULL "
                   + "WHERE username = ? OR email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, usernameOrEmail);
            ps.setString(3, usernameOrEmail);
            ps.executeUpdate();
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User map(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("username"),
            rs.getString("role")
        );
    }
}
