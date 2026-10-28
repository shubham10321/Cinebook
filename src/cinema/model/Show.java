package cinema.model;

import java.sql.Date;
import java.sql.Time;

public class Show {
    private int showId;
    private int movieId;
    private int hallId;
    private String hallName;
    private Date showDate;
    private Time showTime;
    private String movieTitle;

    public Show() {}

    public int getShowId()               { return showId; }
    public void setShowId(int v)         { this.showId = v; }

    public int getMovieId()              { return movieId; }
    public void setMovieId(int v)        { this.movieId = v; }

    public int getHallId()               { return hallId; }
    public void setHallId(int v)         { this.hallId = v; }

    public String getHallName()          { return hallName; }
    public void setHallName(String v)    { this.hallName = v; }

    public Date getShowDate()            { return showDate; }
    public void setShowDate(Date v)      { this.showDate = v; }

    public Time getShowTime()            { return showTime; }
    public void setShowTime(Time v)      { this.showTime = v; }

    public String getMovieTitle()        { return movieTitle; }
    public void setMovieTitle(String v)  { this.movieTitle = v; }

    @Override
    public String toString() {
        if (showTime == null) return "Show " + showId;
        String t = showTime.toString().substring(0, 5);
        return hallName != null ? t + " [" + hallName + "]" : t;
    }
}
