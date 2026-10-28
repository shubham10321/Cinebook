package cinema.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;

import cinema.model.FoodCombo;
import cinema.util.DBConnection;

public class FoodComboDAO {

    public LinkedList<FoodCombo> getAllCombos() throws Exception {
        LinkedList<FoodCombo> list = new LinkedList<>();
        String sql = "SELECT combo_id, name, description, price, emoji FROM food_combos ORDER BY combo_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void addCombo(String name, String description, BigDecimal price, String emoji) throws Exception {
        validate(name, price);
        String sql = "INSERT INTO food_combos (name, description, price, emoji) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setBigDecimal(3, price);
            ps.setString(4, emoji == null || emoji.isBlank() ? "🍿" : emoji);
            ps.executeUpdate();
        }
    }

    public void updateCombo(int comboId, String name, String description, BigDecimal price, String emoji) throws Exception {
        validate(name, price);
        String sql = "UPDATE food_combos SET name=?, description=?, price=?, emoji=? WHERE combo_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setBigDecimal(3, price);
            ps.setString(4, emoji == null || emoji.isBlank() ? "🍿" : emoji);
            ps.setInt(5, comboId);
            ps.executeUpdate();
        }
    }

    public void deleteCombo(int comboId) throws Exception {
        String sql = "DELETE FROM food_combos WHERE combo_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comboId);
            ps.executeUpdate();
        }
    }

    /** Insert booking_combo rows for a completed booking (called inside a transaction). */
    public void saveBookingCombos(Connection conn, int bookingId,
                                  java.util.Map<Integer, Integer> comboQtyMap,
                                  java.util.Map<Integer, BigDecimal> comboPriceMap) throws Exception {
        if (comboQtyMap == null || comboQtyMap.isEmpty()) return;
        String sql = "INSERT INTO booking_combos (booking_id, combo_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (var entry : comboQtyMap.entrySet()) {
                int comboId = entry.getKey();
                int qty     = entry.getValue();
                BigDecimal price = comboPriceMap.getOrDefault(comboId, BigDecimal.ZERO);
                ps.setInt(1, bookingId);
                ps.setInt(2, comboId);
                ps.setInt(3, qty);
                ps.setBigDecimal(4, price.multiply(new BigDecimal(qty)));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private FoodCombo map(ResultSet rs) throws SQLException {
        return new FoodCombo(
            rs.getInt("combo_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            rs.getString("emoji")
        );
    }

    private void validate(String name, BigDecimal price) throws Exception {
        if (name == null || name.isBlank()) throw new Exception("Combo name cannot be empty.");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new Exception("Price must be greater than 0.");
    }
}
