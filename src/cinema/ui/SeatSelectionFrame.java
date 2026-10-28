package cinema.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import cinema.dao.BookingDAO;
import cinema.dao.FoodComboDAO;
import cinema.model.*;
import cinema.util.PdfTicketGenerator;
import cinema.util.UITheme;

/**
 * Seat-selection screen — enhanced with:
 *  - Dynamic hall layout (rows × cols from Hall object)
 *  - Food combo step
 *  - Payment simulation step
 *  - PDF ticket generation
 *  - "Email" ticket (saves to Desktop + shows confirmation dialog)
 */
public class SeatSelectionFrame extends JFrame {

    private static final char[] ROW_LABELS =
        "ABCDEFGHIJKLMNOPQRST".toCharArray();   // up to 20 rows

    private final User    user;
    private final Movie   movie;
    private final Show    show;
    private final Hall    hall;

    private final BookingDAO    bookingDAO    = new BookingDAO();
    private final FoodComboDAO  foodComboDAO  = new FoodComboDAO();

    private final TreeSet<String> selectedSeats = new TreeSet<>();
    private final JLabel          totalLabel    = new JLabel();
    private Set<String>           bookedSeats;

    public SeatSelectionFrame(User user, Movie movie, Show show, Hall hall) {
        this.user  = user;
        this.movie = movie;
        this.show  = show;
        this.hall  = hall;

        int rows = (hall != null) ? hall.getHallRows() : 4;
        int cols = (hall != null) ? hall.getCols()     : 5;

        setTitle("CineBook – Select Seats  |  " + (hall != null ? hall.getName() : ""));
        setSize(Math.max(650, cols * 72 + 120), Math.max(560, rows * 58 + 220));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG);

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(4, 1));
        header.setBackground(UITheme.PANEL);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        header.add(heading(movie.getTitle()));
        header.add(info("Date: " + show.getShowDate() + "    Time: " + show));
        header.add(info("Hall: " + (hall != null ? hall.getName() : "—")
                      + "    Layout: " + rows + " rows × " + cols + " seats"));
        header.add(info("Price per ticket: Rs. " + movie.getTicketPrice()));

        // ── Legend ──────────────────────────────────────────────────────────
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        legend.setBackground(UITheme.BG);
        legend.add(legendItem(UITheme.SUCCESS,  "Available"));
        legend.add(legendItem(UITheme.SELECTED, "Selected"));
        legend.add(legendItem(UITheme.DANGER,   "Booked"));

        // ── Screen label ────────────────────────────────────────────────────
        JLabel screen = new JLabel("◀━━━━━━━━━━  S C R E E N  ━━━━━━━━━━▶", SwingConstants.CENTER);
        screen.setOpaque(true);
        screen.setBackground(new Color(100, 116, 139));
        screen.setForeground(Color.BLACK);
        screen.setFont(UITheme.BUTTON);
        screen.setBorder(new EmptyBorder(8, 8, 8, 8));

        // ── Seat grid ───────────────────────────────────────────────────────
        JPanel seatGrid = new JPanel(new GridLayout(rows, cols, 6, 6));
        seatGrid.setBackground(UITheme.BG);
        seatGrid.setBorder(new EmptyBorder(10, 30, 10, 30));

        try {
            bookedSeats = bookingDAO.getBookedSeats(show.getShowId());
        } catch (Exception e) {
            bookedSeats = Set.of();
            JOptionPane.showMessageDialog(this, "Could not load booked seats.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 1; c <= cols; c++) {
                String seatNo = ROW_LABELS[Math.min(r, ROW_LABELS.length - 1)] + String.valueOf(c);
                seatGrid.add(createSeatButton(seatNo));
            }
        }

        // ── Footer ──────────────────────────────────────────────────────────
        totalLabel.setFont(UITheme.HEADING);
        totalLabel.setForeground(Color.WHITE);
        updateTotal();

        JButton confirmButton = UITheme.successButton("Add Food & Pay →");
        JButton backButton    = UITheme.coloredButton("Back", UITheme.CARD, UITheme.TEXT);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.BG);
        footer.setBorder(new EmptyBorder(10, 20, 16, 20));
        footer.add(totalLabel, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(backButton);
        actions.add(confirmButton);
        footer.add(actions, BorderLayout.EAST);

        // ── Assemble ────────────────────────────────────────────────────────
        JPanel screenWrap = new JPanel(new BorderLayout());
        screenWrap.setBackground(UITheme.BG);
        screenWrap.setBorder(new EmptyBorder(0, 30, 0, 30));
        screenWrap.add(screen, BorderLayout.CENTER);

        JPanel mid = new JPanel(new BorderLayout(0, 10));
        mid.setBackground(UITheme.BG);
        mid.add(screenWrap, BorderLayout.NORTH);
        mid.add(new JScrollPane(seatGrid) {{ getViewport().setBackground(UITheme.BG); setBorder(null); }},
                BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(UITheme.BG);
        center.add(legend, BorderLayout.NORTH);
        center.add(mid,    BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        confirmButton.addActionListener(e -> proceedToFood());
        backButton.addActionListener(e -> {
            new BookingFrame(user, movie).setVisible(true);
            dispose();
        });
    }

    // ─── Seat button ──────────────────────────────────────────────────────────

    private JButton createSeatButton(String seatNo) {
        JButton btn = new JButton(seatNo);
        btn.setFont(UITheme.BUTTON);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boolean alreadyBooked = bookedSeats.contains(seatNo);
        if (alreadyBooked) {
            btn.setBackground(UITheme.DANGER);
            btn.setForeground(Color.WHITE);
            btn.setEnabled(false);
        } else {
            btn.setBackground(UITheme.SUCCESS);
            btn.setForeground(Color.WHITE);
        }
        btn.addActionListener(e -> toggleSeat(seatNo, btn));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setToolTipText(alreadyBooked ? seatNo + " is already booked."
                        : selectedSeats.contains(seatNo) ? "Click to unselect " + seatNo
                        : "Click to select " + seatNo);
            }
        });
        return btn;
    }

    private void toggleSeat(String seatNo, JButton btn) {
        if (bookedSeats.contains(seatNo)) {
            JOptionPane.showMessageDialog(this, "This seat is already booked.",
                    "Seat unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedSeats.contains(seatNo)) {
            selectedSeats.remove(seatNo);
            btn.setBackground(UITheme.SUCCESS);
        } else {
            selectedSeats.add(seatNo);
            btn.setBackground(UITheme.SELECTED);
        }
        updateTotal();
    }

    private void updateTotal() {
        BigDecimal total = movie.getTicketPrice().multiply(new BigDecimal(selectedSeats.size()));
        totalLabel.setText("Seats: " + (selectedSeats.isEmpty() ? "—" : selectedSeats)
                + "    Tickets Total: Rs. " + total);
    }

    // ─── Step 2: Food combos ──────────────────────────────────────────────────

    private void proceedToFood() {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one seat.",
                    "No seat selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.List<FoodCombo> combos;
        try {
            combos = foodComboDAO.getAllCombos();
        } catch (Exception ex) {
            combos = java.util.Collections.emptyList();
        }

        BigDecimal ticketTotal = movie.getTicketPrice().multiply(new BigDecimal(selectedSeats.size()));
        Map<Integer, Integer> comboSelection = java.util.Collections.emptyMap();

        if (!combos.isEmpty()) {
            FoodComboFrame foodFrame = new FoodComboFrame(this, combos, ticketTotal);
            comboSelection = foodFrame.show(this);
        }

        // Build price map for total calculation
        Map<Integer, BigDecimal> comboPriceMap = new LinkedHashMap<>();
        for (FoodCombo fc : combos) comboPriceMap.put(fc.getComboId(), fc.getPrice());

        BigDecimal foodTotal = BigDecimal.ZERO;
        for (var e : comboSelection.entrySet()) {
            BigDecimal unitPrice = comboPriceMap.getOrDefault(e.getKey(), BigDecimal.ZERO);
            foodTotal = foodTotal.add(unitPrice.multiply(new BigDecimal(e.getValue())));
        }
        BigDecimal grandTotal = ticketTotal.add(foodTotal);

        // Step 3: Payment
        proceedToPayment(grandTotal, comboSelection, comboPriceMap);
    }

    // ─── Step 3: Payment ──────────────────────────────────────────────────────

    private void proceedToPayment(BigDecimal grandTotal,
                                  Map<Integer, Integer> comboQtyMap,
                                  Map<Integer, BigDecimal> comboPriceMap) {
        PaymentFrame pf = new PaymentFrame(this, grandTotal, user.getName());
        pf.setVisible(true);

        if (pf.getOutcome() != PaymentFrame.Result.PAID) {
            JOptionPane.showMessageDialog(this, "Payment cancelled. Seats are still available.",
                    "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Commit booking
        try {
            Booking booking = bookingDAO.createBooking(
                    user.getUserId(), show.getShowId(), selectedSeats,
                    movie.getTicketPrice(),
                    comboQtyMap, comboPriceMap,
                    pf.getPaymentMethod(), pf.getTransactionId());
            if (hall != null) booking.setHallName(hall.getName());
            showConfirmation(booking);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Booking failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Confirmation + PDF ───────────────────────────────────────────────────

    private void showConfirmation(Booking booking) {
        String details = "✅  Booking Confirmed!\n\n"
                + "Booking ID  : #" + booking.getBookingId() + "\n"
                + "Customer    : " + booking.getCustomerName() + "\n"
                + "Movie       : " + booking.getMovieTitle() + "\n"
                + "Date        : " + booking.getShowDate() + "\n"
                + "Time        : " + booking.getShowTime() + "\n"
                + "Hall        : " + booking.getHallName() + "\n"
                + "Seats       : " + booking.getSeatList() + "\n"
                + "Food        : " + booking.getFoodSummary() + "\n"
                + "Payment     : " + booking.getPaymentMethod() + "\n"
                + "Txn ID      : " + booking.getTransactionId() + "\n"
                + "Total Paid  : Rs. " + booking.getTotalAmount();

        Object[] options = {"📄 Download PDF Ticket", "📧 'Email' Ticket (Demo)", "Close"};
        int result = JOptionPane.showOptionDialog(this, details, "Booking Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        if (result == 0) downloadPdf(booking);
        else if (result == 1) emailTicket(booking);
    }

    private void downloadPdf(Booking booking) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("CineBook_Ticket_" + booking.getBookingId() + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            PdfTicketGenerator.generate(booking, chooser.getSelectedFile());
            JOptionPane.showMessageDialog(this,
                    "PDF ticket saved to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not create PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void emailTicket(Booking booking) {
        // DEMO: Save PDF to Desktop and tell user "email sent"
        try {
            File desktop = new File(System.getProperty("user.home"), "Desktop");
            File out = new File(desktop, "CineBook_Ticket_" + booking.getBookingId() + ".pdf");
            PdfTicketGenerator.generate(booking, out);
            JOptionPane.showMessageDialog(this,
                    "📧  DEMO MODE — Email Simulation\n\n"
                    + "In a real app, the ticket PDF would be emailed.\n"
                    + "The ticket has been saved to your Desktop:\n"
                    + out.getAbsolutePath(),
                    "Ticket 'Emailed'", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not generate PDF: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Legend / Label helpers ───────────────────────────────────────────────

    private JPanel legendItem(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JLabel box = new JLabel("  ");
        box.setOpaque(true);
        box.setBackground(color);
        box.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel lbl = new JLabel(text);
        lbl.setForeground(UITheme.TEXT);
        p.add(box); p.add(lbl);
        return p;
    }

    private JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.HEADING);
        l.setForeground(Color.WHITE);
        return l;
    }

    private JLabel info(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.NORMAL);
        l.setForeground(UITheme.MUTED);
        return l;
    }
}
