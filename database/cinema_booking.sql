-- CineBook - Cinema Ticket Booking System
-- Run this script in MySQL to create the database, tables, and sample data.

CREATE DATABASE IF NOT EXISTS cinebook;
USE cinebook;

DROP TABLE IF EXISTS booking_seats;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS shows;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER'
);

CREATE TABLE movies (
    movie_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    genre VARCHAR(50) NOT NULL,
    duration INT NOT NULL,
    ticket_price DECIMAL(8,2) NOT NULL,
    CONSTRAINT chk_duration CHECK (duration > 0),
    CONSTRAINT chk_price CHECK (ticket_price > 0)
);

CREATE TABLE shows (
    show_id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    CONSTRAINT fk_show_movie FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_movie_show UNIQUE (movie_id, show_date, show_time)
);

CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    show_id INT NOT NULL,
    total_seats INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    booking_status ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booking_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES shows(show_id)
);

CREATE TABLE booking_seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    show_id INT NOT NULL,
    seat_number VARCHAR(5) NOT NULL,
    CONSTRAINT fk_seat_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_seat_show FOREIGN KEY (show_id) REFERENCES shows(show_id),
    CONSTRAINT uq_show_seat UNIQUE (show_id, seat_number)
);

-- Demo admin account. Password is hashed with SHA-256 (not stored as plain text).
-- Sample login username: admin
-- Sample login password: admin123
INSERT INTO users (name, username, password_hash, role)
VALUES ('Cinema Admin', 'admin', SHA2('admin123', 256), 'ADMIN');

-- Optional demo customer
-- Sample login username: student
-- Sample login password: student123
INSERT INTO users (name, username, password_hash, role)
VALUES ('Demo Student', 'student', SHA2('student123', 256), 'CUSTOMER');

INSERT INTO movies (title, genre, duration, ticket_price) VALUES
('The Dark Knight', 'Action', 152, 250.00),
('Inception', 'Sci-Fi', 148, 280.00),
('Interstellar', 'Sci-Fi', 169, 300.00),
('Frozen', 'Animation', 102, 180.00),
('Avengers: Endgame', 'Action', 181, 320.00);

-- Sample shows for today and the next two days
INSERT INTO shows (movie_id, show_date, show_time)
SELECT movie_id, CURDATE(), '10:00:00' FROM movies
UNION ALL
SELECT movie_id, CURDATE(), '14:30:00' FROM movies
UNION ALL
SELECT movie_id, CURDATE(), '19:00:00' FROM movies
UNION ALL
SELECT movie_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00:00' FROM movies
UNION ALL
SELECT movie_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '18:00:00' FROM movies
UNION ALL
SELECT movie_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00' FROM movies
UNION ALL
SELECT movie_id, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '21:00:00' FROM movies;
