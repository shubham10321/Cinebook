# CineBook Project Report

**Title:** CineBook - Cinema Ticket Booking System  
**Subject:** Advanced Java  
**Type:** Desktop application (Java Swing + MySQL)

## 1. Problem Statement

A cinema needs a simple computer-based system so that customers can look at movies, choose a show, pick seats, and confirm a booking. Staff should be able to manage the movie list and see booking records. The system must not allow the same seat to be sold twice for the same show, and it should show clear messages when the user makes a mistake.

## 2. Project Scope

The project covers login, registration, movie listing, show selection, seat selection, booking, cancellation, and a small admin dashboard. It does not include real payment, a website, or an external movie API.

## 3. System Requirements

- Windows PC
- JDK 17+
- MySQL Server
- MySQL JDBC driver (`lib/mysql-connector-j-9.3.0.jar`)

## 4. System Architecture

Layers used in the code:

1. **UI layer** (`cinema.ui`) - windows and buttons
2. **DAO layer** (`cinema.dao`) - SQL using JDBC
3. **Model layer** (`cinema.model`) - simple data classes
4. **Utility layer** (`cinema.util`) - connection, hashing, colours
5. **Database** - MySQL schema in `database/cinema_booking.sql`

This is not a full enterprise architecture. It is only a clear split so a student can explain each package.

## 5. Data Flow

1. User enters username and password.
2. `UserDAO` reads the hash from `users` and compares it.
3. Customer sees movies from `MovieDAO`.
4. Booking screen reads dates and times from `ShowDAO`.
5. Seat screen reads booked seats from `BookingDAO`.
6. Confirm writes `bookings` and `booking_seats` in one transaction.

## 6. ER Diagram

See the mermaid ER diagram in `README.md`. In short:

- One user can have many bookings.
- One movie can have many shows.
- One show can have many bookings.
- One booking has many seats.
- A seat number is unique for a given show.

## 7. Algorithm for Ticket Booking

1. Select movie.
2. Select date.
3. Select showtime.
4. Load occupied seats for that show only.
5. Select one or more free seats (`TreeSet`).
6. Show total price.
7. Confirm.
8. `BEGIN` transaction.
9. Insert booking.
10. Insert seats.
11. `COMMIT` or `ROLLBACK`.
12. Display booking ID and allow save to a text file.

## 8. Advanced Java Concepts Used

| Concept | Where it is used |
|---------|------------------|
| Swing | Frames, buttons, tables, dialogs |
| AWT layouts | BorderLayout, GridLayout, GridBagLayout, FlowLayout |
| ActionListener | Login, Register, Book Ticket, Confirm, Admin buttons |
| KeyListener | Enter to login; movie search box |
| MouseListener | Seat button hover tooltips |
| JDBC | All DAO classes |
| TreeSet | Selected seat numbers |
| LinkedList | Movie list and booking history |
| Exception handling | Empty fields, SQL errors, duplicate seats |

## 9. Testing Table

| ID | Feature | Input | Result |
|----|---------|-------|--------|
| T1 | Login validation | Empty fields | Warning dialog |
| T2 | Login | Wrong password | Invalid credentials |
| T3 | Register | Existing username | Duplicate username |
| T4 | Movie search | Letters in search box | Filtered table |
| T5 | Seat colours | Free / selected / booked | Green / blue / red |
| T6 | Booking | Valid seats | Row in bookings + booking_seats |
| T7 | Unique seat | Same seat, same show | SQL unique error, rollback |
| T8 | Cancel | Confirmed future booking | Seats removed, status CANCELLED |
| T9 | Admin add movie | Valid form | Movie + default shows inserted |
| T10 | Admin invalid price | 0 or letters | Error, no insert |

Compilation on the development PC succeeded with `javac` and JDK 27. Full GUI tests need a configured MySQL login in `db.properties`.

## 10. Conclusion

The application is a complete student-level cinema booking system. The main learning outcome is connecting a Swing GUI to MySQL and protecting seat bookings with a unique constraint plus a transaction.

## 11. Extra Viva Questions

**Q. What is Nimbus?**  
A look-and-feel that makes Swing look a little more modern. `UITheme.apply()` selects it if it is available.

**Q. Why not hardcode the database password in Java?**  
Anyone who opens the source would see it. The project reads `db.properties` or environment variables instead.

**Q. What is `autoCommit(false)`?**  
It means several SQL statements are treated as one unit. Either all of them succeed, or none of them stay in the database.

**Q. Can seat A1 be booked for two different shows?**  
Yes. The unique key is `(show_id, seat_number)`, not the seat number alone.

**Q. Why ENUM for role and booking_status?**  
MySQL then allows only the listed values, which matches the two roles and two booking states used in the program.

**Q. How would you explain this project in 1 minute?**  
CineBook is a Java Swing desktop app. Customers log in, pick a movie and show, choose seats, and save a ticket. Admins manage movies. Data is stored in MySQL through JDBC. Duplicate seats are blocked by a unique key and a transaction.
