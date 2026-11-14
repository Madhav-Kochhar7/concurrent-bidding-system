# Online Auction System

A college project demonstrating **Multithreading**, **Concurrency Control (`synchronized`)**, and **JDBC Persistence (MySQL)** using Plain Java — without external frameworks (no Spring, Maven, or complex DAOs).

---

## 1. Project Overview

The program models a live auction where:
- Items have a starting price and a minimum bid increment.
- Multiple threads (automated bidder bots) place concurrent bids simultaneously.
- An independent timer thread counts down and closes the auction when time expires.
- All bids and final results are saved to and read from a MySQL database.

### Key Concepts Demonstrated
1. **Threads & Concurrency**: Bidders and countdown timers execute concurrently as separate threads.
2. **Synchronization (`synchronized`, `wait()`, `notifyAll()`)**: Ensures atomic updates and prevents lost bids or race conditions.
3. **JDBC API (`PreparedStatement`, Batch Updates, Auto-generated Keys)**: Connects to and persists relational data in MySQL.
4. **Java Collections**:
   - `ArrayList` for ordered, resizable bid histories.
   - `HashMap` for aggregating bid counts and determining the most active bidder.

---

## 2. Project Structure

```
simple-auction/
├── src/
│   ├── Item.java                 # Item domain model (id, name, prices, winner)
│   ├── Bid.java                  # Bid domain model (bidder, amount)
│   ├── Auction.java              # Thread-safe auction engine with synchronized methods
│   ├── BidderBot.java            # Runnable simulating an autonomous bidder
│   ├── AuctionTimer.java         # Runnable countdown timer that closes the auction
│   ├── InvalidBidException.java  # Custom checked exception for rejected bids
│   ├── DBHelper.java             # Centralized JDBC connection and SQL helper
│   └── Main.java                 # Interactive console menu and execution coordinator
├── lib/
│   └── mysql-connector-j-*.jar   # Place MySQL JDBC driver jar here
├── schema.sql                    # MySQL database and table definitions
├── .gitignore                    # Git ignore file
└── README.md                     # Documentation and viva preparation notes
```

---

## 3. Database Schema

Two tables in database `auction_db`:
- `items`: Stores `item_id`, `name`, `start_price`, `min_increment`, `winner`, and `final_price`.
- `bids`: Stores `bid_id`, `item_id` (foreign key), `bidder_name`, `amount`, and `placed_at` timestamp.

---

## 4. Setup & Running

### Step 1: Initialize Database
```bash
mysql -u root -p < schema.sql
```

### Step 2: Add MySQL Connector JAR
Download the MySQL Connector/J driver `.jar` and place it in the `lib/` directory.

### Step 3: Configure Credentials
Edit the constants at the top of `src/DBHelper.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

### Step 4: Compile & Run

#### On Linux / macOS:
```bash
# Compile
javac -cp "lib/*:src" src/*.java

# Run
java -cp "lib/*:src" Main
```

#### On Windows:
```cmd
# Compile
javac -cp "lib/*;src" src/*.java

# Run
java -cp "lib/*;src" Main
```

*(Note: The project runs completely even without MySQL connected; database operations will gracefully display notices).*

---

## 5. Menu Options

```
=================================
      Online Auction System      
=================================

--- Main Menu ---
1. Create an item and start manual auction
2. Run auction on an item (with 5 bots)
3. Show all items and results
4. Show bid history of an item
0. Exit
```

---

## 6. Sample Output

### Option 2: Bot Auction with Most Active Bidder Report
```
Starting auction for 10 seconds with 5 bots...
Item: Gaming Laptop (Start: 500, Min Increment: 50)
[Bot] Bot-2 placed bid: 592
[Bot] Bot-4 placed bid: 682
...
[Timer] Time's up (10s)! Closing auction...

=================================
         AUCTION CLOSED          
=================================
Item        : Gaming Laptop
Winner      : Bot-1
Final Price : 16870
Total bids placed: 163

--- Bidder Activity Report (HashMap) ---
Most Active Bidder: Bot-1 (36 bids)
Breakdown by Bidder:
  - Bot-1: 36 bid(s)
  - Bot-2: 32 bid(s)
  - Bot-3: 33 bid(s)
  - Bot-4: 31 bid(s)
  - Bot-5: 31 bid(s)
```

---

