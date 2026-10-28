package cinema.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import cinema.dao.BookingDAO;
import cinema.model.Booking;
import cinema.model.User;
import cinema.util.UITheme;

public class MyBookingsFrame extends JFrame {

    private final User user;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final DefaultTableModel tableModel;
    private LinkedList<Booking> bookings = new LinkedList<>();

    public MyBookingsFrame(User user) {
        this.user = user;
        setTitle("CineBook - My Bookings");
        setSize(900, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG);

        JLabel title = new JLabel("My Bookings");
        title.setFont(UITheme.HEADING);
        title.setForeground(Color.WHITE);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PANEL);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        header.add(title, BorderLayout.WEST);

        String[] columns = {"Booking ID", "Movie", "Date", "Time", "Seats", "Tickets", "Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UITheme.CARD);

        JButton detailsButton = UITheme.primaryButton("View Details");
        JButton cancelButton = UITheme.dangerButton("Cancel Booking");
        JButton closeButton = UITheme.coloredButton("Close", UITheme.CARD, UITheme.TEXT);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footer.setBackground(UITheme.BG);
        footer.add(detailsButton);
        footer.add(cancelButton);
        footer.add(closeButton);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        loadBookings();

        detailsButton.addActionListener(e -> {
            Booking booking = selectedBooking(table);
            if (booking != null) {
                JOptionPane.showMessageDialog(this, detailsText(booking),
                        "Booking details", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        cancelButton.addActionListener(e -> cancelSelected(table));
        closeButton.addActionListener(e -> dispose());
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        try {
            bookings = bookingDAO.getBookingsByUser(user.getUserId());
            for (Booking booking : bookings) {
                tableModel.addRow(new Object[] {
                        booking.getBookingId(),
                        booking.getMovieTitle(),
                        booking.getShowDate(),
                        booking.getShowTime(),
                        booking.getSeatList(),
                        booking.getTotalSeats(),
                        "Rs. " + booking.getTotalAmount(),
                        booking.getBookingStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load bookings.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Booking selectedBooking(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking.",
                    "No booking selected", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        for (Booking booking : bookings) {
            if (booking.getBookingId() == id) {
                return booking;
            }
        }
        return null;
    }

    private void cancelSelected(JTable table) {
        Booking booking = selectedBooking(table);
        if (booking == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel booking #" + booking.getBookingId() + " and release the seats?",
                "Cancel booking", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            bookingDAO.cancelBooking(booking.getBookingId(), user.getUserId());
            JOptionPane.showMessageDialog(this, "Booking cancelled. The seats are available again.",
                    "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Cannot cancel", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String detailsText(Booking booking) {
        return "Booking ID: " + booking.getBookingId()
                + "\nCustomer: " + booking.getCustomerName()
                + "\nMovie: " + booking.getMovieTitle()
                + "\nShow date: " + booking.getShowDate()
                + "\nShow time: " + booking.getShowTime()
                + "\nSeats: " + booking.getSeatList()
                + "\nTickets: " + booking.getTotalSeats()
                + "\nTotal price: Rs. " + booking.getTotalAmount()
                + "\nStatus: " + booking.getBookingStatus();
    }
}
