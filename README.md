# Online Auction System

A college project demonstrating **Multithreading**, **Concurrency Control (`synchronized`)**, and **JDBC Persistence (MySQL)** using Plain Java — without external frameworks (no Spring, Maven, or complex DAOs).

---

## 1. Project Overview

The program models a live auction where:
- Items have a starting price and a minimum bid increment.
- Multiple threads (automated bidder bots) place concurrent bids simultaneously.
- An independent timer thread counts down and closes the auction when time expires.
- All bids and final results are saved to and read from a MySQL database.
- A built-in race condition demonstration visually proves the necessity of thread synchronization.

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
5. Race condition demo
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

### Option 5: Race Condition Demonstration
```
==================================================
          RACE CONDITION DEMONSTRATION            
==================================================
>>> PART 1: Running with 'synchronized' REMOVED (Unsafe Mode) <<<
Launching 10 concurrent bots for 2 seconds...
[Timer] Time's up (2s)! Closing auction...
Total bids recorded: 758
  [RACE VIOLATION] Bid #5 by Bot-2 was 168 (should have been >= 177 after previous bid of 167)
  [RACE VIOLATION] Bid #6 by Bot-6 was 168 (should have been >= 178 after previous bid of 168)
>>> RESULT: RACE CONDITION: invalid bids were accepted! (182 violations detected) <<<

>>> PART 2: Running WITH 'synchronized' (Safe Mode) <<<
Launching 10 concurrent bots for 2 seconds...
[Timer] Time's up (2s)! Closing auction...
Total bids recorded: 784
>>> RESULT: Safe version passed: all bids were valid and strictly increasing! <<<
```

---

## 7. Viva Preparation Questions & Answers

### Q1: What is a race condition and where did it happen in your demo?
**Answer**: A race condition occurs when two or more concurrent threads access shared mutable state simultaneously, and the final outcome depends on the non-deterministic order of thread execution. 
In our project, when `synchronized` was removed in Option 5, Bot-1 and Bot-2 both read the current highest bid at the exact same time. Both validated that their bid was high enough based on that stale value. During the 1ms execution delay, both wrote to `highestBid` and appended to `history`, causing lower or out-of-order bids to be accepted.

### Q2: Why is `placeBid()` `synchronized`?
**Answer**: Marking `placeBid()` as `synchronized` ensures **mutual exclusion** (atomicity). It acquires the intrinsic monitor lock of the `Auction` object. Only one thread can execute inside `placeBid()` at any given instant. While one thread validates and updates `highestBid` and `history`, all other bidding threads are blocked until the lock is released.

### Q3: Why `while (open) wait();` instead of `if (open) wait();`?
**Answer**:
1. **Spurious Wakeups**: Under POSIX and OS thread implementations, a waiting thread can wake up without any thread calling `notify()` or `notifyAll()`.
2. **State Changes**: Even if awakened by `notifyAll()`, by the time this thread re-acquires the monitor lock, another thread might have changed the condition.
A `while` loop re-checks the condition after waking up, ensuring the thread only proceeds when `open == false`.

### Q4: What do `join()` and `interrupt()` do?
**Answer**:
- `join()`: Pauses the calling thread (e.g. main thread) until the target thread finishes execution (`run()` completes). In our project, `join()` ensures all bots and the timer stop before printing the winner.
- `interrupt()`: Sets the interrupt flag on a thread to signal it should stop. If the thread is blocked in `Thread.sleep()` or `wait()`, an `InterruptedException` is thrown, allowing cooperative termination.

### Q5: Why `PreparedStatement` instead of `Statement`?
**Answer**:
1. **SQL Injection Prevention**: User values are bound as parameters (`?`) rather than concatenated, ensuring malicious inputs cannot alter query logic.
2. **Performance (Pre-compilation)**: The database parses and compiles the SQL statement once; repeated queries with different parameters reuse the cached query execution plan.
3. **Type Safety & Escaping**: Automatically handles formatting and escaping for strings, quotes, and timestamps.

### Q6: What is the difference between `Runnable` and `Thread`?
**Answer**:
- `Thread` is a class that manages execution on an OS thread.
- `Runnable` is a functional interface representing a task (with a single `run()` method).
Implementing `Runnable` is preferred because Java does not support multiple inheritance, and it cleanly decouples the task logic from the execution mechanism.
