package cinema.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;

import cinema.model.Movie;
import cinema.util.DBConnection;

public class MovieDAO {

    public LinkedList<Movie> getAllMovies() throws Exception {
        LinkedList<Movie> movies = new LinkedList<>();
        String sql = "SELECT movie_id, title, genre, duration, ticket_price, description, rating FROM movies ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) movies.add(map(rs));
        }
        return movies;
    }

    public Movie getMovieById(int movieId) throws Exception {
        String sql = "SELECT movie_id, title, genre, duration, ticket_price, description, rating FROM movies WHERE movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void addMovie(String title, String genre, int duration, BigDecimal price,
                         String description, double rating) throws Exception {
        String sql = "INSERT INTO movies (title, genre, duration, ticket_price, description, rating) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, genre);
            ps.setInt(3, duration);
            ps.setBigDecimal(4, price);
            ps.setString(5, description);
            ps.setDouble(6, rating);
            ps.executeUpdate();
        }
    }

    /** Overload kept for backward compatibility. */
    public void addMovie(String title, String genre, int duration, BigDecimal price) throws Exception {
        addMovie(title, genre, duration, price, "", 0.0);
    }

    public void updateMovie(int movieId, String title, String genre, int duration,
                            BigDecimal price, String description, double rating) throws Exception {
        String sql = "UPDATE movies SET title=?, genre=?, duration=?, ticket_price=?, description=?, rating=? WHERE movie_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, genre);
            ps.setInt(3, duration);
            ps.setBigDecimal(4, price);
            ps.setString(5, description);
            ps.setDouble(6, rating);
            ps.setInt(7, movieId);
            ps.executeUpdate();
        }
    }

    public void updateMovie(int movieId, String title, String genre, int duration, BigDecimal price) throws Exception {
        updateMovie(movieId, title, genre, duration, price, "", 0.0);
    }

    public void deleteMovie(int movieId) throws Exception {
        String sql = "DELETE FROM movies WHERE movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ps.executeUpdate();
        }
    }

    private Movie map(ResultSet rs) throws SQLException {
        return new Movie(
            rs.getInt("movie_id"),
            rs.getString("title"),
            rs.getString("genre"),
            rs.getInt("duration"),
            rs.getBigDecimal("ticket_price"),
            rs.getString("description"),
            rs.getDouble("rating")
        );
    }
}
