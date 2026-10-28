package cinema.model;

import java.math.BigDecimal;

public class Movie {
    private int movieId;
    private String title;
    private String genre;
    private int duration;
    private BigDecimal ticketPrice;
    private String description;
    private double rating;

    public Movie() {}

    public Movie(int movieId, String title, String genre, int duration,
                 BigDecimal ticketPrice, String description, double rating) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.ticketPrice = ticketPrice;
        this.description = description;
        this.rating = rating;
    }

    public int getMovieId()                  { return movieId; }
    public void setMovieId(int v)            { this.movieId = v; }

    public String getTitle()                 { return title; }
    public void setTitle(String v)           { this.title = v; }

    public String getGenre()                 { return genre; }
    public void setGenre(String v)           { this.genre = v; }

    public int getDuration()                 { return duration; }
    public void setDuration(int v)           { this.duration = v; }

    public BigDecimal getTicketPrice()       { return ticketPrice; }
    public void setTicketPrice(BigDecimal v) { this.ticketPrice = v; }

    public String getDescription()           { return description; }
    public void setDescription(String v)     { this.description = v; }

    public double getRating()                { return rating; }
    public void setRating(double v)          { this.rating = v; }

    @Override
    public String toString() { return title; }
}
