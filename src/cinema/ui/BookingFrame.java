package cinema.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Date;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import cinema.dao.ShowDAO;
import cinema.model.Movie;
import cinema.model.Show;
import cinema.model.User;
import cinema.util.UITheme;

public class BookingFrame extends JFrame {

    private final User user;
    private final Movie movie;
    private final ShowDAO showDAO = new ShowDAO();
    private final JComboBox<Date> dateBox = new JComboBox<>();
    private final JComboBox<Show> timeBox = new JComboBox<>();

    public BookingFrame(User user, Movie movie) {
        this.user = user;
        this.movie = movie;
        setTitle("CineBook - Book Ticket");
        setSize(520, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG);

        JPanel header = new JPanel(new GridLayout(5, 1, 4, 4));
        header.setBackground(UITheme.PANEL);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));
        header.add(heading("Select Show"));
        header.add(info("Movie: " + movie.getTitle()));
        header.add(info("Genre: " + movie.getGenre()));
        header.add(info("Duration: " + movie.getDuration() + " minutes"));
        header.add(info("Ticket price: Rs. " + movie.getTicketPrice()));

        JPanel form = new JPanel(new GridLayout(4, 1, 8, 8));
        form.setBackground(UITheme.BG);
        form.setBorder(new EmptyBorder(18, 22, 18, 22));
        form.add(UITheme.mutedLabel("Show date"));
        form.add(dateBox);
        form.add(UITheme.mutedLabel("Show time"));
        form.add(timeBox);

        JButton continueButton = UITheme.successButton("Choose Seats");
        JButton backButton = UITheme.coloredButton("Close", UITheme.CARD, UITheme.TEXT);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(UITheme.BG);
        buttons.setBorder(new EmptyBorder(0, 22, 16, 22));
        buttons.add(backButton);
        buttons.add(continueButton);

        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        loadDates();
        dateBox.addActionListener(e -> loadTimes());
        continueButton.addActionListener(e -> openSeats());
        backButton.addActionListener(e -> dispose());
    }

    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.HEADING);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JLabel info(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.NORMAL);
        label.setForeground(UITheme.TEXT);
        return label;
    }

    private void loadDates() {
        dateBox.removeAllItems();
        try {
            LinkedList<Date> dates = showDAO.getDatesForMovie(movie.getMovieId());
            if (dates.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No upcoming shows are available for this movie.",
                        "No shows", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            for (Date date : dates) {
                dateBox.addItem(date);
            }
            loadTimes();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load show dates.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTimes() {
        timeBox.removeAllItems();
        Date date = (Date) dateBox.getSelectedItem();
        if (date == null) {
            return;
        }
        try {
            LinkedList<Show> shows = showDAO.getShows(movie.getMovieId(), date);
            for (Show show : shows) {
                timeBox.addItem(show);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load showtimes.\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSeats() {
        Show show = (Show) timeBox.getSelectedItem();
        if (show == null) {
            JOptionPane.showMessageDialog(this, "Please select a show date and time.",
                    "Missing show", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new SeatSelectionFrame(user, movie, show).setVisible(true);
        dispose();
    }
}
