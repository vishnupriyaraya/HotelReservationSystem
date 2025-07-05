# Hotel Reservation System

A simple Java-based Hotel Reservation System using SQLite as the backend database.

## Features

- Add, manage, and retrieve hotel room details
- Handle reservations for different room types
- Persistent storage using SQLite database
- Basic command-line interface for interaction

## Project Structure

├── DBConnection.java # Handles database connectivity
├── Hotel.java # Hotel logic and room management
├── Room.java # Room details and classification
├── Reservation.java # Reservation data structure
├── Main.java # Entry point of the application
├── hotel.db # SQLite database file
├── sqlite-jdbc-3.36.0.3.jar # SQLite JDBC driver


## Requirements

- Java JDK 8 or higher
- SQLite JDBC driver (included)

## How to Run

1. **Compile the Java files:**
   ```bash
   javac -cp .:sqlite-jdbc-3.36.0.3.jar *.java

2. **Run the application:**
   java -cp .;sqlite-jdbc-3.36.0.3.jar Main
