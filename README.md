# Online Auction System

A simple console-based online auction program written in plain Java demonstrating multi-threading, concurrency control with `synchronized`, and JDBC persistence with MySQL.

## Project Structure

```
simple-auction/
├── src/
│   ├── Item.java
│   ├── Bid.java
│   ├── Auction.java
│   ├── BidderBot.java
│   ├── AuctionTimer.java
│   ├── InvalidBidException.java
│   ├── DBHelper.java
│   └── Main.java
├── lib/
│   └── mysql-connector-j-*.jar (place JAR here)
├── schema.sql
├── .gitignore
└── README.md
```

## Prerequisites

- Java Development Kit (JDK 8 or higher)
- MySQL Server (5.7+ or 8.0+)
- MySQL Connector/J driver `.jar` file

---

## MySQL Setup Steps

### 1. Initialize the Database and Tables
Run `schema.sql` using the MySQL CLI or any database client (such as MySQL Workbench):

```bash
mysql -u root -p < schema.sql
```

This creates the database `auction_db` and two tables:
- `items`: Stores item details, starting price, minimum increment, winner, and final price.
- `bids`: Stores every placed bid with item ID, bidder name, bid amount, and timestamp.

### 2. Add MySQL Connector JAR
1. Download the [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (or from Maven Central, e.g. `mysql-connector-j-8.4.0.jar`).
2. Place the `.jar` file inside the `lib/` directory.

### 3. Configure Database Credentials
Open `src/DBHelper.java` and edit the connection constants at the top to match your local MySQL configuration:

```java
private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

> **Note**: Do not commit your real password to version control.

---

## How to Compile and Run

### On Linux / macOS:
```bash
# Compile with MySQL driver on classpath
javac -cp "lib/*:src" src/*.java

# Run the program
java -cp "lib/*:src" Main
```

### On Windows:
```cmd
# Compile with MySQL driver on classpath
javac -cp "lib/*;src" src/*.java

# Run the program
java -cp "lib/*;src" Main
```
