package cinema.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import cinema.dao.MovieDAO;
import cinema.model.Movie;
import cinema.model.User;
import cinema.util.UITheme;

public class HomeFrame extends JFrame {

    private final User user;
    private final MovieDAO movieDAO = new MovieDAO();
    private final DefaultTableModel tableModel;
    private final JTable movieTable;
    private LinkedList<Movie> movies = new LinkedList<>();

    public HomeFrame(User user) {
        this.user = user;
        setTitle("CineBook - Home");
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PANEL);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel appName = new JLabel("CineBook");
        appName.setFont(UITheme.TITLE);
        appName.setForeground(Color.WHITE);

        JLabel welcome = new JLabel("Welcome, " + user.getName());
        welcome.setFont(UITheme.NORMAL);
        welcome.setForeground(UITheme.MUTED);

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        titleBox.add(appName, BorderLayout.NORTH);
        titleBox.add(welcome, BorderLayout.SOUTH);
        header.add(titleBox, BorderLayout.WEST);

        JButton bookingsButton = UITheme.primaryButton("My Bookings");
        JButton logoutButton = UITheme.dangerButton("Logout");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(bookingsButton);
        right.add(logoutButton);
        header.add(right, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(UITheme.BG);
        center.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        JLabel moviesLabel = new JLabel("Available Movies");
        moviesLabel.setFont(UITheme.HEADING);
        moviesLabel.setForeground(Color.WHITE);
        JTextField searchField = new JTextField();
        UITheme.styleField(searchField);
        searchField.setToolTipText("Type a movie name to filter the list");
        searchRow.add(moviesLabel, BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);

        String[] columns = {"ID", "Movie Name", "Genre", "Duration (min)", "Ticket Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        movieTable = new JTable(tableModel);
        UITheme.styleTable(movieTable);
        movieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(movieTable);
        scroll.getViewport().setBackground(UITheme.CARD);

        JButton bookButton = UITheme.successButton("Book Ticket");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(bookButton);

        center.add(searchRow, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        loadMovies();

        // KeyListener: filter movies as the student types
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                fillTable(searchField.getText().trim());
            }
        });

        bookButton.addActionListener(e -> openBooking());
        bookingsButton.addActionListener(e -> new MyBookingsFrame(user).setVisible(true));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void loadMovies() {
        try {
            movies = movieDAO.getAllMovies();
            fillTable("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load movies.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTable(String filter) {
        tableModel.setRowCount(0);
        String q = filter.toLowerCase();
        for (Movie movie : movies) {
            if (!q.isEmpty() && !movie.getTitle().toLowerCase().contains(q)
                    && !movie.getGenre().toLowerCase().contains(q)) {
                continue;
            }
            tableModel.addRow(new Object[] {
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getGenre(),
                    movie.getDuration(),
                    "Rs. " + movie.getTicketPrice()
            });
        }
    }

    private void openBooking() {
        int row = movieTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a movie first.",
                    "No movie selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int movieId = (int) tableModel.getValueAt(row, 0);
        Movie selected = null;
        for (Movie movie : movies) {
            if (movie.getMovieId() == movieId) {
                selected = movie;
                break;
            }
        }
        if (selected != null) {
            new BookingFrame(user, selected).setVisible(true);
        }
    }
}
