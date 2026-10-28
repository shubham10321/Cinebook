package cinema.dao;

import java.sql.*;
import java.util.LinkedList;

import cinema.model.Hall;
import cinema.util.DBConnection;

public class HallDAO {

    public LinkedList<Hall> getAllHalls() throws Exception {
        LinkedList<Hall> halls = new LinkedList<>();
        String sql = "SELECT hall_id, name, hall_rows, cols FROM halls ORDER BY hall_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                halls.add(map(rs));
            }
        }
        return halls;
    }

    public Hall getHallById(int hallId) throws Exception {
        String sql = "SELECT hall_id, name, hall_rows, cols FROM halls WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void addHall(String name, int rows, int cols) throws Exception {
        validate(name, rows, cols);
        String sql = "INSERT INTO halls (name, hall_rows, cols) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, rows);
            ps.setInt(3, cols);
            ps.executeUpdate();
        }
    }

    public void updateHall(int hallId, String name, int rows, int cols) throws Exception {
        validate(name, rows, cols);
        String sql = "UPDATE halls SET name = ?, hall_rows = ?, cols = ? WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, rows);
            ps.setInt(3, cols);
            ps.setInt(4, hallId);
            ps.executeUpdate();
        }
    }

    public void deleteHall(int hallId) throws Exception {
        String sql = "DELETE FROM halls WHERE hall_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hallId);
            ps.executeUpdate();
        }
    }

    private Hall map(ResultSet rs) throws SQLException {
        return new Hall(
            rs.getInt("hall_id"),
            rs.getString("name"),
            rs.getInt("hall_rows"),
            rs.getInt("cols")
        );
    }

    private void validate(String name, int rows, int cols) throws Exception {
        if (name == null || name.isBlank())       throw new Exception("Hall name cannot be empty.");
        if (rows < 1 || rows > 20)                throw new Exception("Rows must be between 1 and 20.");
        if (cols < 1 || cols > 20)                throw new Exception("Columns must be between 1 and 20.");
    }
}
