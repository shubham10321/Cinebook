package cinema.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import cinema.dao.BookingDAO;
import cinema.dao.MovieDAO;
import cinema.model.Booking;
import cinema.model.Movie;
import cinema.model.User;
import cinema.util.UITheme;

public class AdminDashboard extends JFrame {

    private final MovieDAO movieDAO = new MovieDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final DefaultTableModel movieModel;
    private final DefaultTableModel bookingModel;
    private final JLabel totalBookingsLabel = new JLabel();
    private final JLabel totalSalesLabel = new JLabel();
    private LinkedList<Movie> movies = new LinkedList<>();

    public AdminDashboard(User admin) {
        setTitle("CineBook - Admin Dashboard");
        setSize(980, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PANEL);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("CineBook Admin");
        title.setFont(UITheme.TITLE);
        title.setForeground(Color.WHITE);
        JLabel welcome = new JLabel("Logged in as " + admin.getName());
        welcome.setForeground(UITheme.MUTED);
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(welcome);
        header.add(left, BorderLayout.WEST);
        JButton logout = UITheme.dangerButton("Logout");
        header.add(logout, BorderLayout.EAST);

        String[] movieCols = {"ID", "Title", "Genre", "Duration", "Price"};
        movieModel = new DefaultTableModel(movieCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable movieTable = new JTable(movieModel);
        UITheme.styleTable(movieTable);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTextField titleField = new JTextField();
        JTextField genreField = new JTextField();
        JTextField durationField = new JTextField();
        JTextField priceField = new JTextField();
        UITheme.styleField(titleField);
        UITheme.styleField(genreField);
        UITheme.styleField(durationField);
        UITheme.styleField(priceField);

        JPanel form = new JPanel(new GridLayout(1, 4, 8, 8));
        form.setBackground(UITheme.BG);
        form.add(labeled("Title", titleField));
        form.add(labeled("Genre", genreField));
        form.add(labeled("Duration (min)", durationField));
        form.add(labeled("Ticket price", priceField));

        JButton addBtn = UITheme.successButton("Add Movie");
        JButton updateBtn = UITheme.primaryButton("Update Movie");
        JButton deleteBtn = UITheme.dangerButton("Delete Movie");
        JPanel movieButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        movieButtons.setBackground(UITheme.BG);
        movieButtons.add(addBtn);
        movieButtons.add(updateBtn);
        movieButtons.add(deleteBtn);

        JPanel moviesTab = new JPanel(new BorderLayout(8, 8));
        moviesTab.setBackground(UITheme.BG);
        moviesTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        moviesTab.add(form, BorderLayout.NORTH);
        moviesTab.add(new JScrollPane(movieTable), BorderLayout.CENTER);
        moviesTab.add(movieButtons, BorderLayout.SOUTH);

        String[] bookingCols = {"ID", "Customer", "Movie", "Date", "Time", "Seats", "Amount", "Status"};
        bookingModel = new DefaultTableModel(bookingCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable bookingTable = new JTable(bookingModel);
        UITheme.styleTable(bookingTable);

        totalBookingsLabel.setFont(UITheme.HEADING);
        totalBookingsLabel.setForeground(Color.WHITE);
        totalSalesLabel.setFont(UITheme.HEADING);
        totalSalesLabel.setForeground(Color.WHITE);
        JPanel stats = new JPanel(new GridLayout(1, 2, 12, 0));
        stats.setBackground(UITheme.PANEL);
        stats.setBorder(new EmptyBorder(12, 12, 12, 12));
        stats.add(totalBookingsLabel);
        stats.add(totalSalesLabel);

        JPanel bookingsTab = new JPanel(new BorderLayout(8, 8));
        bookingsTab.setBackground(UITheme.BG);
        bookingsTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        bookingsTab.add(stats, BorderLayout.NORTH);
        bookingsTab.add(new JScrollPane(bookingTable), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Movies", moviesTab);
        tabs.addTab("All Bookings", bookingsTab);

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadMovies();
        loadBookings();

        movieTable.getSelectionModel().addListSelectionListener(e -> {
            int row = movieTable.getSelectedRow();
            if (row >= 0) {
                titleField.setText(String.valueOf(movieModel.getValueAt(row, 1)));
                genreField.setText(String.valueOf(movieModel.getValueAt(row, 2)));
                durationField.setText(String.valueOf(movieModel.getValueAt(row, 3)));
                priceField.setText(String.valueOf(movieModel.getValueAt(row, 4)));
            }
        });

        addBtn.addActionListener(e -> {
            try {
                MovieData data = readMovieForm(titleField, genreField, durationField, priceField);
                movieDAO.addMovie(data.title, data.genre, data.duration, data.price);
                JOptionPane.showMessageDialog(this, "Movie added.");
                clearForm(titleField, genreField, durationField, priceField);
                loadMovies();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot add movie",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        updateBtn.addActionListener(e -> {
            int row = movieTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a movie to update.");
                return;
            }
            try {
                int id = (int) movieModel.getValueAt(row, 0);
                MovieData data = readMovieForm(titleField, genreField, durationField, priceField);
                movieDAO.updateMovie(id, data.title, data.genre, data.duration, data.price);
                JOptionPane.showMessageDialog(this, "Movie updated.");
                loadMovies();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot update movie",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = movieTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a movie to delete.");
                return;
            }
            int id = (int) movieModel.getValueAt(row, 0);
            int ok = JOptionPane.showConfirmDialog(this, "Delete this movie?", "Delete",
                    JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                movieDAO.deleteMovie(id);
                JOptionPane.showMessageDialog(this, "Movie deleted.");
                clearForm(titleField, genreField, durationField, priceField);
                loadMovies();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot delete movie",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private JPanel labeled(String text, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(UITheme.mutedLabel(text), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void loadMovies() {
        movieModel.setRowCount(0);
        try {
            movies = movieDAO.getAllMovies();
            for (Movie movie : movies) {
                movieModel.addRow(new Object[] {
                        movie.getMovieId(),
                        movie.getTitle(),
                        movie.getGenre(),
                        movie.getDuration(),
                        movie.getTicketPrice()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load movies.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBookings() {
        bookingModel.setRowCount(0);
        try {
            LinkedList<Booking> bookings = bookingDAO.getAllBookings();
            for (Booking booking : bookings) {
                bookingModel.addRow(new Object[] {
                        booking.getBookingId(),
                        booking.getCustomerName(),
                        booking.getMovieTitle(),
                        booking.getShowDate(),
                        booking.getShowTime(),
                        booking.getSeatList(),
                        "Rs. " + booking.getTotalAmount(),
                        booking.getBookingStatus()
                });
            }
            totalBookingsLabel.setText("Total bookings: " + bookingDAO.getTotalBookings());
            totalSalesLabel.setText("Total ticket sales: Rs. " + bookingDAO.getTotalSales());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load bookings.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private MovieData readMovieForm(JTextField titleField, JTextField genreField,
                                    JTextField durationField, JTextField priceField) throws Exception {
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        String durationText = durationField.getText().trim();
        String priceText = priceField.getText().trim();
        if (title.isEmpty() || genre.isEmpty() || durationText.isEmpty() || priceText.isEmpty()) {
            throw new Exception("Please fill in all movie details.");
        }
        int duration;
        try {
            duration = Integer.parseInt(durationText);
        } catch (NumberFormatException e) {
            throw new Exception("Duration must be a whole number of minutes.");
        }
        if (duration <= 0) {
            throw new Exception("Duration must be greater than 0.");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException e) {
            throw new Exception("Ticket price must be a valid number.");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Ticket price must be greater than 0.");
        }
        return new MovieData(title, genre, duration, price);
    }

    private void clearForm(JTextField... fields) {
        for (JTextField field : fields) {
            field.setText("");
        }
    }

    private static class MovieData {
        final String title;
        final String genre;
        final int duration;
        final BigDecimal price;

        MovieData(String title, String genre, int duration, BigDecimal price) {
            this.title = title;
            this.genre = genre;
            this.duration = duration;
            this.price = price;
        }
    }
}
