package cinema.util;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Reads MySQL settings from db.properties or environment variables
 * and opens a JDBC connection.
 */
public class DBConnection {

    public static Connection getConnection() throws Exception {
        String url = System.getenv("CINEBOOK_DB_URL");
        String user = System.getenv("CINEBOOK_DB_USER");
        String password = System.getenv("CINEBOOK_DB_PASSWORD");

        File configFile = findConfigFile();
        if (configFile != null) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
            }
            if (url == null || url.isBlank()) {
                url = props.getProperty("db.url");
            }
            if (user == null || user.isBlank()) {
                user = props.getProperty("db.user");
            }
            if (password == null) {
                password = props.getProperty("db.password", "");
            }
        }

        if (url == null || url.isBlank() || user == null || user.isBlank()) {
            throw new Exception(
                    "Database settings are missing.\n"
                    + "Copy db.properties.example to db.properties and enter your MySQL details,\n"
                    + "or set CINEBOOK_DB_URL, CINEBOOK_DB_USER and CINEBOOK_DB_PASSWORD.");
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    private static File findConfigFile() {
        File current = new File("db.properties");
        if (current.exists()) {
            return current;
        }
        File parent = new File("..", "db.properties");
        if (parent.exists()) {
            return parent;
        }
        return null;
    }
}
