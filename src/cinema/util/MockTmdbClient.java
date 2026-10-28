package cinema.util;

import java.math.BigDecimal;
import java.util.*;

/**
 * Mock TMDB client — DEMO MODE.
 *
 * Returns hardcoded movie metadata so no real API key is needed.
 * In production, replace the body of {@link #search(String)} with
 * an HTTP call to https://api.themoviedb.org/3/search/movie?api_key=KEY&query=TITLE
 */
public class MockTmdbClient {

    public static class TmdbMovie {
        public final String title;
        public final String genre;
        public final int    duration;
        public final double rating;
        public final String description;
        public final BigDecimal suggestedPrice;

        TmdbMovie(String title, String genre, int duration, double rating,
                  String description, double price) {
            this.title = title;
            this.genre = genre;
            this.duration = duration;
            this.rating = rating;
            this.description = description;
            this.suggestedPrice = BigDecimal.valueOf(price);
        }
    }

    // ── Hardcoded catalog (simulates TMDB responses) ──────────────────────────

    private static final List<TmdbMovie> CATALOG = List.of(
        new TmdbMovie("The Dark Knight",          "Action",    152, 9.0, "Batman faces the Joker in Gotham.", 250),
        new TmdbMovie("Inception",                "Sci-Fi",    148, 8.8, "A thief enters people's dreams.", 280),
        new TmdbMovie("Interstellar",             "Sci-Fi",    169, 8.6, "Astronauts seek a new home.", 300),
        new TmdbMovie("Frozen",                   "Animation", 102, 7.4, "A princess searches for her sister.", 180),
        new TmdbMovie("Avengers: Endgame",        "Action",    181, 8.4, "Final battle against Thanos.", 320),
        new TmdbMovie("Dune",                     "Sci-Fi",    155, 8.0, "A noble family on a desert planet.", 290),
        new TmdbMovie("Top Gun: Maverick",        "Action",    131, 8.3, "Maverick returns to the skies.", 270),
        new TmdbMovie("The Lion King",            "Animation", 118, 8.5, "Young lion reclaims his throne.", 200),
        new TmdbMovie("Doctor Strange",           "Action",    115, 7.5, "Surgeon becomes a sorcerer.", 260),
        new TmdbMovie("Oppenheimer",              "Drama",     180, 8.9, "Father of the atomic bomb.", 310),
        new TmdbMovie("Barbie",                   "Comedy",    114, 6.9, "Barbie ventures into the real world.", 220),
        new TmdbMovie("Spider-Man: No Way Home",  "Action",    148, 8.2, "Peter Parker's identity revealed.", 280),
        new TmdbMovie("The Batman",               "Action",    176, 7.8, "Bruce Wayne investigates Gotham crime.", 270),
        new TmdbMovie("Black Panther",            "Action",    134, 7.3, "T'Challa defends Wakanda.", 240),
        new TmdbMovie("Parasite",                 "Thriller",  132, 8.6, "A poor family infiltrates the rich.", 260),
        new TmdbMovie("RRR",                      "Action",    182, 7.9, "Two Indian legends fight colonizers.", 250),
        new TmdbMovie("KGF: Chapter 2",           "Action",    168, 8.2, "Rocky dominates the gold mines.", 280),
        new TmdbMovie("Pathaan",                  "Action",    146, 5.9, "Ex-spy returns to protect India.", 230),
        new TmdbMovie("Jawan",                    "Action",    169, 7.3, "A prison warden takes on a mission.", 260),
        new TmdbMovie("Animal",                   "Drama",     201, 7.1, "A man's obsession with his father.", 270)
    );

    /**
     * Search the mock catalog for movies whose title contains the query (case-insensitive).
     *
     * @param query search string
     * @return matching TmdbMovie list (up to 5 results)
     */
    public static List<TmdbMovie> search(String query) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        String q = query.toLowerCase();
        List<TmdbMovie> results = new ArrayList<>();
        for (TmdbMovie m : CATALOG) {
            if (m.title.toLowerCase().contains(q) || m.genre.toLowerCase().contains(q)) {
                results.add(m);
                if (results.size() >= 5) break;
            }
        }
        return results;
    }

    /** Return all movies in the catalog (for browsing). */
    public static List<TmdbMovie> all() {
        return CATALOG;
    }
}
