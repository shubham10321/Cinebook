# CineBook — MySQL Workbench Commands
### Database Verification Reference

---

## Step 1 — Select the Database

```sql
USE cinebook;
```

---

## Step 2 — List All Tables

```sql
SHOW TABLES;
```

**Expected Output:**

| Tables_in_cinebook |
|--------------------|
| booking_seats      |
| bookings           |
| movies             |
| shows              |
| users              |

---

## Step 3 — View All Data in Each Table

```sql
SELECT * FROM users;
```

```sql
SELECT * FROM movies;
```

```sql
SELECT * FROM shows;
```

```sql
SELECT * FROM bookings;
```

```sql
SELECT * FROM booking_seats;
```

---

## Step 4 — Check Table Structure (Columns & Data Types)

```sql
DESCRIBE users;
```

```sql
DESCRIBE movies;
```

```sql
DESCRIBE shows;
```

```sql
DESCRIBE bookings;
```

```sql
DESCRIBE booking_seats;
```

---

## Step 5 — Check Foreign Keys & Constraints

```sql
SHOW CREATE TABLE shows;
```

```sql
SHOW CREATE TABLE bookings;
```

```sql
SHOW CREATE TABLE booking_seats;
```

---

## Step 6 — Count Rows in All Tables (Quick Summary)

```sql
SELECT 'users'         AS table_name, COUNT(*) AS total_rows FROM users
UNION ALL
SELECT 'movies',        COUNT(*) FROM movies
UNION ALL
SELECT 'shows',         COUNT(*) FROM shows
UNION ALL
SELECT 'bookings',      COUNT(*) FROM bookings
UNION ALL
SELECT 'booking_seats', COUNT(*) FROM booking_seats;
```

---

## Step 7 — Check Confirmed Bookings Only

```sql
SELECT * FROM bookings
WHERE booking_status = 'CONFIRMED';
```

---

## Step 8 — Check Total Sales Amount

```sql
SELECT COUNT(*) AS total_bookings,
       SUM(total_amount) AS total_sales
FROM bookings
WHERE booking_status = 'CONFIRMED';
```

---

## Step 9 — Check Which Seats Are Booked for a Show

```sql
SELECT bs.seat_number, b.booking_id, u.name, m.title, s.show_date, s.show_time
FROM booking_seats bs
JOIN bookings b ON bs.booking_id = b.booking_id
JOIN users u ON b.user_id = u.user_id
JOIN shows s ON bs.show_id = s.show_id
JOIN movies m ON s.movie_id = m.movie_id
ORDER BY s.show_date, bs.seat_number;
```

---

## Step 10 — Check Admin Account Exists

```sql
SELECT user_id, name, username, role
FROM users
WHERE role = 'ADMIN';
```

---

## How to Run in MySQL Workbench

1. Open **MySQL Workbench**
2. Connect to your local MySQL server
3. Click **File → New Query Tab**
4. Paste any command from above
5. Press **Ctrl + Enter** to run the selected query
6. Or press the **⚡ lightning bolt** button to run all

---

*Database name: `cinebook` | MySQL version: 8.0 / 8.4*
