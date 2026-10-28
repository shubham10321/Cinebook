-- CineBook v2 - Enhanced Cinema Ticket Booking System
-- Run this to upgrade to the full-featured schema.
-- Safe to run fresh (drops and recreates everything).

CREATE DATABASE IF NOT EXISTS cinebook;
USE cinebook;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS booking_combos;
DROP TABLE IF EXISTS food_combos;
DROP TABLE IF EXISTS booking_seats;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS shows;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS halls;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- ─── HALLS ────────────────────────────────────────────────────────────────────
CREATE TABLE halls (
    hall_id   INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(60)  NOT NULL,
    hall_rows INT          NOT NULL DEFAULT 6,
    cols      INT          NOT NULL DEFAULT 8,
    CONSTRAINT chk_hall_rows CHECK (hall_rows BETWEEN 1 AND 20),
    CONSTRAINT chk_hall_cols CHECK (cols BETWEEN 1 AND 20)
);

-- ─── USERS ────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(80)  NOT NULL,           -- bcrypt hash (~60 chars) or SHA-256 (64)
    email         VARCHAR(150),
    role          ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    otp_code      VARCHAR(6),
    otp_expiry    DATETIME
);

-- ─── MOVIES ───────────────────────────────────────────────────────────────────
CREATE TABLE movies (
    movie_id     INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    genre        VARCHAR(50)  NOT NULL,
    duration     INT          NOT NULL,
    ticket_price DECIMAL(8,2) NOT NULL,
    description  TEXT,
    rating       DECIMAL(3,1) DEFAULT 0.0,
    CONSTRAINT chk_duration CHECK (duration > 0),
    CONSTRAINT chk_price    CHECK (ticket_price > 0)
);

-- ─── SHOWS ────────────────────────────────────────────────────────────────────
CREATE TABLE shows (
    show_id   INT AUTO_INCREMENT PRIMARY KEY,
    movie_id  INT  NOT NULL,
    hall_id   INT  NOT NULL DEFAULT 1,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    CONSTRAINT fk_show_movie FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
    CONSTRAINT fk_show_hall  FOREIGN KEY (hall_id)  REFERENCES halls(hall_id),
    CONSTRAINT uq_movie_show UNIQUE (movie_id, hall_id, show_date, show_time)
);

-- ─── FOOD COMBOS ──────────────────────────────────────────────────────────────
CREATE TABLE food_combos (
    combo_id    INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(200),
    price       DECIMAL(8,2)  NOT NULL,
    emoji       VARCHAR(10)   DEFAULT '🍿'
);

-- ─── BOOKINGS ─────────────────────────────────────────────────────────────────
CREATE TABLE bookings (
    booking_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT          NOT NULL,
    show_id         INT          NOT NULL,
    total_seats     INT          NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    booking_status  ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booking_date    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method  VARCHAR(30)  DEFAULT 'CASH',
    payment_status  VARCHAR(20)  DEFAULT 'PAID',
    transaction_id  VARCHAR(50),
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES shows(show_id)
);

-- ─── BOOKING SEATS ────────────────────────────────────────────────────────────
CREATE TABLE booking_seats (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    booking_id  INT         NOT NULL,
    show_id     INT         NOT NULL,
    seat_number VARCHAR(5)  NOT NULL,
    CONSTRAINT fk_seat_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_seat_show    FOREIGN KEY (show_id)    REFERENCES shows(show_id),
    CONSTRAINT uq_show_seat    UNIQUE (show_id, seat_number)
);

-- ─── BOOKING COMBOS ───────────────────────────────────────────────────────────
CREATE TABLE booking_combos (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT          NOT NULL,
    combo_id   INT          NOT NULL,
    quantity   INT          NOT NULL DEFAULT 1,
    subtotal   DECIMAL(8,2) NOT NULL,
    CONSTRAINT fk_bc_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    CONSTRAINT fk_bc_combo   FOREIGN KEY (combo_id)   REFERENCES food_combos(combo_id)
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- SEED DATA
-- ═══════════════════════════════════════════════════════════════════════════════

-- Halls
INSERT INTO halls (name, hall_rows, cols) VALUES
('Hall 1 - Gold',    6, 8),
('Hall 2 - Platinum',8, 10),
('Hall 3 - IMAX',    10, 12);

-- Demo users (SHA-256 hashes — reset to bcrypt after first login via app)
-- admin / admin123
INSERT INTO users (name, username, password_hash, email, role)
VALUES ('Cinema Admin', 'admin', SHA2('admin123', 256), 'admin@cinebook.demo', 'ADMIN');

-- student / student123
INSERT INTO users (name, username, password_hash, email, role)
VALUES ('Demo Student', 'student', SHA2('student123', 256), 'student@cinebook.demo', 'CUSTOMER');

-- Movies with descriptions and ratings
INSERT INTO movies (title, genre, duration, ticket_price, description, rating) VALUES
('The Dark Knight',    'Action',    152, 250.00, 'Batman faces the Joker in a battle for Gotham''s soul.',          9.0),
('Inception',          'Sci-Fi',    148, 280.00, 'A thief enters dreams to plant an idea in a CEO''s mind.',        8.8),
('Interstellar',       'Sci-Fi',    169, 300.00, 'A team of astronauts travel through a wormhole near Saturn.',     8.6),
('Frozen',             'Animation', 102, 180.00, 'A fearless princess sets off to find her ice-powered sister.',    7.4),
('Avengers: Endgame',  'Action',    181, 320.00, 'The Avengers assemble for one final battle against Thanos.',      8.4),
('Dune',               'Sci-Fi',    155, 290.00, 'A noble family''s son is destined to free a desert planet.',      8.0),
('Top Gun: Maverick',  'Action',    131, 270.00, 'Maverick pushes limits as a top aviator instructor.',             8.3),
('The Lion King',      'Animation', 118, 200.00, 'A young lion prince flees his kingdom after his father''s death.',8.5),
('Doctor Strange',     'Action',    115, 260.00, 'A surgeon becomes a powerful sorcerer after an accident.',        7.5),
('Oppenheimer',        'Drama',     180, 310.00, 'The story of J. Robert Oppenheimer and the atomic bomb.',         8.9);

-- Shows (today + 2 days, all 3 halls)
INSERT INTO shows (movie_id, hall_id, show_date, show_time)
SELECT movie_id, 1, CURDATE(), '10:00:00' FROM movies UNION ALL
SELECT movie_id, 1, CURDATE(), '14:30:00' FROM movies UNION ALL
SELECT movie_id, 2, CURDATE(), '17:00:00' FROM movies UNION ALL
SELECT movie_id, 2, CURDATE(), '20:30:00' FROM movies UNION ALL
SELECT movie_id, 3, CURDATE(), '11:00:00' FROM movies UNION ALL
SELECT movie_id, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00' FROM movies UNION ALL
SELECT movie_id, 2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:30:00' FROM movies UNION ALL
SELECT movie_id, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00' FROM movies UNION ALL
SELECT movie_id, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00:00' FROM movies UNION ALL
SELECT movie_id, 3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '21:00:00' FROM movies;

-- Food combos
INSERT INTO food_combos (name, description, price, emoji) VALUES
('Classic Popcorn',    'Large salted or caramel popcorn bucket',                  120.00, '🍿'),
('Mega Meal Deal',     'Large popcorn + any 2 drinks + nachos',                   350.00, '🍔'),
('Drinks Combo',       '2 large soft drinks of your choice',                      150.00, '🥤'),
('Nachos Special',     'Loaded nachos with cheese dip and salsa',                 180.00, '🌮'),
('Ice Cream Tub',      'Premium 2-scoop ice cream cup',                            90.00, '🍦'),
('Family Feast',       'XL popcorn + 4 drinks + nachos + 2 ice creams',           600.00, '🎉');

SELECT 'CineBook v2 database setup complete!' AS Status;
