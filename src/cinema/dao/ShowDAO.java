package cinema.dao;

import java.sql.*;
import java.util.LinkedList;

import cinema.model.Hall;
import cinema.model.Show;
import cinema.util.DBConnection;

public class ShowDAO {

    public LinkedList<Show> getShowsByMovie(int movieId) throws Exception {
        LinkedList<Show> shows = new LinkedList<>();
        String sql = "SELECT s.show_id, s.movie_id, s.hall_id, s.show_date, s.show_time, "
                   + "h.name AS hall_name "
                   + "FROM shows s "
                   + "LEFT JOIN halls h ON s.hall_id = h.hall_id "
                   + "WHERE s.movie_id = ? AND s.show_date >= CURDATE() "
                   + "ORDER BY s.show_date, s.show_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Show show = new Show();
                    show.setShowId(rs.getInt("show_id"));
                    show.setMovieId(rs.getInt("movie_id"));
                    show.setHallId(rs.getInt("hall_id"));
                    show.setShowDate(rs.getDate("show_date"));
                    show.setShowTime(rs.getTime("show_time"));
                    show.setHallName(rs.getString("hall_name"));
                    shows.add(show);
                }
            }
        }
        return shows;
    }

    public LinkedList<String> getDistinctDates(int movieId) throws Exception {
        LinkedList<String> dates = new LinkedList<>();
        String sql = "SELECT DISTINCT show_date FROM shows WHERE movie_id = ? AND show_date >= CURDATE() ORDER BY show_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) dates.add(rs.getDate(1).toString());
            }
        }
        return dates;
    }
}
