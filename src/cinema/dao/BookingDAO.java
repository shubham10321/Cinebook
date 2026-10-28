package cinema.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import cinema.model.Booking;
import cinema.util.DBConnection;

public class BookingDAO {

    // ─── Read booked seats ────────────────────────────────────────────────────

    public Set<String> getBookedSeats(int showId) throws Exception {
        Set<String> seats = new HashSet<>();
        String sql = "SELECT seat_number FROM booking_seats WHERE show_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) seats.add(rs.getString("seat_number"));
            }
        }
        return seats;
    }

    // ─── Create booking ───────────────────────────────────────────────────────

    /**
     * Create a booking with optional food combos and payment details.
     *
     * @param userId          logged-in user
     * @param showId          selected show
     * @param selectedSeats   TreeSet of seat labels (e.g. A1, B3)
     * @param ticketPrice     price per ticket
     * @param comboQtyMap     comboId → quantity  (may be null/empty)
     * @param comboPriceMap   comboId → unit price (may be null/empty)
     * @param paymentMethod   "Card", "UPI", "Cash" etc.
     * @param transactionId   fake txn ID from payment simulator
     */
    public Booking createBooking(int userId, int showId, TreeSet<String> selectedSeats,
                                 BigDecimal ticketPrice,
                                 Map<Integer, Integer> comboQtyMap,
                                 Map<Integer, BigDecimal> comboPriceMap,
                                 String paymentMethod, String transactionId) throws Exception {

        if (selectedSeats == null || selectedSeats.isEmpty())
            throw new Exception("Please select at least one seat.");

        BigDecimal ticketTotal = ticketPrice.multiply(new BigDecimal(selectedSeats.size()));

        // Add food combo totals
        BigDecimal foodTotal = BigDecimal.ZERO;
        if (comboQtyMap != null) {
            for (var entry : comboQtyMap.entrySet()) {
                BigDecimal unitPrice = comboPriceMap.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                foodTotal = foodTotal.add(unitPrice.multiply(new BigDecimal(entry.getValue())));
            }
        }
        BigDecimal grandTotal = ticketTotal.add(foodTotal);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert booking
            String bookingSql = "INSERT INTO bookings (user_id, show_id, total_seats, total_amount, "
                    + "booking_status, payment_method, payment_status, transaction_id) "
                    + "VALUES (?, ?, ?, ?, 'CONFIRMED', ?, 'PAID', ?)";
            int bookingId;
            try (PreparedStatement ps = conn.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setInt(2, showId);
                ps.setInt(3, selectedSeats.size());
                ps.setBigDecimal(4, grandTotal);
                ps.setString(5, paymentMethod == null ? "Cash" : paymentMethod);
                ps.setString(6, transactionId == null ? "DEMO-" + System.currentTimeMillis() : transactionId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new Exception("Could not create booking.");
                    bookingId = keys.getInt(1);
                }
            }

            // Insert seats
            String seatSql = "INSERT INTO booking_seats (booking_id, show_id, seat_number) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(seatSql)) {
                for (String seat : selectedSeats) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, showId);
                    ps.setString(3, seat);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Insert food combos
            if (comboQtyMap != null && !comboQtyMap.isEmpty()) {
                new FoodComboDAO().saveBookingCombos(conn, bookingId, comboQtyMap, comboPriceMap);
            }

            conn.commit();
            return getBookingById(bookingId);

        } catch (SQLIntegrityConstraintViolationException e) {
            rollbackQuietly(conn);
            throw new Exception("One or more selected seats were just booked by someone else. Please choose again.");
        } catch (Exception e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            closeConn(conn);
        }
    }

    /** Backward-compatible overload (no combos, no payment info). */
    public Booking createBooking(int userId, int showId, TreeSet<String> selectedSeats,
                                 BigDecimal ticketPrice) throws Exception {
        return createBooking(userId, showId, selectedSeats, ticketPrice,
                null, null, "Cash", null);
    }

    // ─── Fetch bookings ───────────────────────────────────────────────────────

    public LinkedList<Booking> getBookingsByUser(int userId) throws Exception {
        String sql = baseSql() + "WHERE b.user_id = ? ORDER BY b.booking_date DESC";
        return loadBookings(sql, userId);
    }

    public LinkedList<Booking> getAllBookings() throws Exception {
        return loadBookings(baseSql() + "ORDER BY b.booking_date DESC", null);
    }

    public LinkedList<Booking> getBookingsByDateRange(java.sql.Date from, java.sql.Date to) throws Exception {
        String sql = baseSql()
                + "WHERE DATE(b.booking_date) BETWEEN ? AND ? ORDER BY b.booking_date DESC";
        LinkedList<Booking> bookings = new LinkedList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bookings.add(mapRow(conn, rs));
            }
        }
        return bookings;
    }

    public Booking getBookingById(int bookingId) throws Exception {
        String sql = baseSql() + "WHERE b.booking_id = ?";
        LinkedList<Booking> list = loadBookings(sql, bookingId);
        return list.isEmpty() ? null : list.getFirst();
    }

    // ─── Stats ────────────────────────────────────────────────────────────────

    public int getTotalBookings() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM bookings WHERE booking_status='CONFIRMED'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public BigDecimal getTotalSales() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COALESCE(SUM(total_amount),0) FROM bookings WHERE booking_status='CONFIRMED'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    public void cancelBooking(int bookingId, int userId) throws Exception {
        Booking booking = getBookingById(bookingId);
        if (booking == null) throw new Exception("Booking not found.");
        if (booking.getUserId() != userId) throw new Exception("You can cancel only your own bookings.");
        if (!"CONFIRMED".equalsIgnoreCase(booking.getBookingStatus()))
            throw new Exception("This booking is already cancelled.");
        java.sql.Date showDate = java.sql.Date.valueOf(booking.getShowDate());
        java.sql.Date today    = java.sql.Date.valueOf(java.time.LocalDate.now());
        if (showDate.before(today)) throw new Exception("Past shows cannot be cancelled.");

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM booking_seats WHERE booking_id = ?")) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE bookings SET booking_status='CANCELLED' WHERE booking_id = ?")) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (Exception e) {
            rollbackQuietly(conn);
            throw e;
        } finally {
            closeConn(conn);
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private String baseSql() {
        return "SELECT b.booking_id, b.user_id, b.show_id, b.total_seats, b.total_amount, "
             + "b.booking_status, b.booking_date, b.payment_method, b.payment_status, b.transaction_id, "
             + "u.name, m.title, s.show_date, s.show_time, h.name AS hall_name "
             + "FROM bookings b "
             + "JOIN users u  ON b.user_id  = u.user_id "
             + "JOIN shows s  ON b.show_id  = s.show_id "
             + "JOIN movies m ON s.movie_id = m.movie_id "
             + "LEFT JOIN halls h ON s.hall_id = h.hall_id ";
    }

    private LinkedList<Booking> loadBookings(String sql, Integer idParam) throws Exception {
        LinkedList<Booking> bookings = new LinkedList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idParam != null) ps.setInt(1, idParam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bookings.add(mapRow(conn, rs));
            }
        }
        return bookings;
    }

    private Booking mapRow(Connection conn, ResultSet rs) throws Exception {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setShowId(rs.getInt("show_id"));
        b.setTotalSeats(rs.getInt("total_seats"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setBookingStatus(rs.getString("booking_status"));
        b.setBookingDate(rs.getTimestamp("booking_date"));
        b.setPaymentMethod(rs.getString("payment_method"));
        b.setPaymentStatus(rs.getString("payment_status"));
        b.setTransactionId(rs.getString("transaction_id"));
        b.setCustomerName(rs.getString("name"));
        b.setMovieTitle(rs.getString("title"));
        b.setShowDate(rs.getDate("show_date").toString());
        String time = rs.getTime("show_time").toString();
        b.setShowTime(time.length() >= 5 ? time.substring(0, 5) : time);
        b.setHallName(rs.getString("hall_name"));
        b.setSeats(loadSeats(conn, b.getBookingId()));
        b.setFoodCombos(loadFoodCombos(conn, b.getBookingId()));
        return b;
    }

    private LinkedList<String> loadSeats(Connection conn, int bookingId) throws Exception {
        LinkedList<String> seats = new LinkedList<>();
        String sql = "SELECT seat_number FROM booking_seats WHERE booking_id = ? ORDER BY seat_number";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) seats.add(rs.getString("seat_number"));
            }
        }
        return seats;
    }

    private Map<String, Integer> loadFoodCombos(Connection conn, int bookingId) throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT fc.name, bc.quantity FROM booking_combos bc "
                   + "JOIN food_combos fc ON bc.combo_id = fc.combo_id "
                   + "WHERE bc.booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) map.put(rs.getString("name"), rs.getInt("quantity"));
            }
        }
        return map;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) { try { conn.rollback(); } catch (Exception ignored) {} }
    }

    private void closeConn(Connection conn) {
        if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {} }
    }
}
