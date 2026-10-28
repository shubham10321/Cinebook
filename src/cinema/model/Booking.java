package cinema.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;

public class Booking {
    private int bookingId;
    private int userId;
    private int showId;
    private int totalSeats;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private Timestamp bookingDate;
    private String customerName;
    private String movieTitle;
    private String showDate;
    private String showTime;
    private String hallName;
    private String paymentMethod  = "CASH";
    private String paymentStatus  = "PAID";
    private String transactionId;
    private LinkedList<String> seats = new LinkedList<>();
    /** comboName -> quantity */
    private Map<String, Integer> foodCombos = new LinkedHashMap<>();

    // ─── getters / setters ────────────────────────────────────────────────────

    public int getBookingId()               { return bookingId; }
    public void setBookingId(int v)         { this.bookingId = v; }

    public int getUserId()                  { return userId; }
    public void setUserId(int v)            { this.userId = v; }

    public int getShowId()                  { return showId; }
    public void setShowId(int v)            { this.showId = v; }

    public int getTotalSeats()              { return totalSeats; }
    public void setTotalSeats(int v)        { this.totalSeats = v; }

    public BigDecimal getTotalAmount()      { return totalAmount; }
    public void setTotalAmount(BigDecimal v){ this.totalAmount = v; }

    public String getBookingStatus()        { return bookingStatus; }
    public void setBookingStatus(String v)  { this.bookingStatus = v; }

    public Timestamp getBookingDate()       { return bookingDate; }
    public void setBookingDate(Timestamp v) { this.bookingDate = v; }

    public String getCustomerName()         { return customerName; }
    public void setCustomerName(String v)   { this.customerName = v; }

    public String getMovieTitle()           { return movieTitle; }
    public void setMovieTitle(String v)     { this.movieTitle = v; }

    public String getShowDate()             { return showDate; }
    public void setShowDate(String v)       { this.showDate = v; }

    public String getShowTime()             { return showTime; }
    public void setShowTime(String v)       { this.showTime = v; }

    public String getHallName()             { return hallName; }
    public void setHallName(String v)       { this.hallName = v; }

    public String getPaymentMethod()        { return paymentMethod; }
    public void setPaymentMethod(String v)  { this.paymentMethod = v; }

    public String getPaymentStatus()        { return paymentStatus; }
    public void setPaymentStatus(String v)  { this.paymentStatus = v; }

    public String getTransactionId()        { return transactionId; }
    public void setTransactionId(String v)  { this.transactionId = v; }

    public LinkedList<String> getSeats()    { return seats; }
    public void setSeats(LinkedList<String> v){ this.seats = v; }

    public Map<String, Integer> getFoodCombos()           { return foodCombos; }
    public void setFoodCombos(Map<String, Integer> v)     { this.foodCombos = v; }

    public String getSeatList() { return String.join(", ", seats); }

    public String getFoodSummary() {
        if (foodCombos.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : foodCombos.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(e.getKey()).append(" x").append(e.getValue());
        }
        return sb.toString();
    }
}
