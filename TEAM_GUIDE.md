# Team Guide: Online Auction System

Welcome team! This document explains our project in simple terms so everyone on the team understands how it works, how to run it on your own laptop, and how to answer questions during the project viva/presentation.

---

## 1. Project at a Glance (The 30-Second Summary)

- **What it is**: A console-based Online Auction System built in **Plain Java** with **JDBC (MySQL)**.
- **No Heavy Frameworks**: No Spring, No Maven, No Gradle. Just pure, foundational Java.
- **Core Features**:
  1. **Manual Bidding**: Users can create an item and type bids in real time.
  2. **Multi-threaded Bot Auction**: 5 automated bots (`BidderBot` threads) place concurrent bids simultaneously while an `AuctionTimer` thread counts down 10 seconds.
  3. **Thread Safety**: Uses Java's `synchronized` keyword so two bots can never corrupt the highest bid at the same millisecond.
  4. **Database Persistence**: Every item, bid, and final winner is stored in MySQL using JDBC `PreparedStatement`.
  5. **Analytics Report**: Uses a `HashMap` to find which bidder was the most active.

---

## 2. How the Program Works (Mental Model)

```
                       +-------------------+
                       |    Main Thread    |
                       +---------+---------+
                                 |
        +------------------------+------------------------+
        | Starts 5 Bots                                   | Starts 1 Timer
        v                                                 v
+---------------+                                 +---------------+
|  BidderBot 1  |                                 |  AuctionTimer |
|  BidderBot 2  |                                 |   (10 secs)   |
|  BidderBot 3  |                                 +-------+-------+
|  BidderBot 4  |                                         |
|  BidderBot 5  |                                         | Sleeps 10s, then
+-------+-------+                                         | calls auction.close()
        |                                                 |
        | Calls synchronized placeBid()                   |
        v                                                 v
+-----------------------------------------------------------------+
|                         Auction Object                          |
|  - Item details (Name, Start Price, Minimum Increment)          |
|  - Current Highest Bid (Shared Mutable State)                   |
|  - Bid History (ArrayList<Bid>)                                 |
|  - open (true / false)                                          |
+--------------------------------+--------------------------------+
                                 |
                                 | Bids & Results saved via JDBC
                                 v
                     +-----------------------+
                     |   MySQL Database      |
                     |   - items table       |
                     |   - bids table        |
                     +-----------------------+
```

1. You start the auction (e.g. Gaming Laptop, starting price 500, min increment 50).
2. The item is saved to MySQL, and an `item_id` is generated.
3. 5 `BidderBot` threads start running concurrently.
4. Each bot sleeps for a random time (100–500 ms), reads the current required minimum bid, and calls `auction.placeBid()`.
5. Because `placeBid()` is `synchronized`, Java only lets **one bot inside the method at a time**.
6. The `AuctionTimer` thread sleeps for 10 seconds, then calls `auction.close()`.
7. `close()` wakes up the main thread (`notifyAll()`), prints the winner, and saves all bids to MySQL in batch.

---

## 3. The 8 Java Files Explained

We deliberately kept the project small and clean — exactly 8 files:

| File | Type | Purpose |
|---|---|---|
| `Item.java` | POJO / Model | Stores item data: `id`, `name`, `startPrice`, `minIncrement`, `winner`, `finalPrice`. |
| `Bid.java` | POJO / Model | Stores a single bid: `bidder` (name) and `amount`. |
| `InvalidBidException.java` | Custom Exception | Checked exception thrown when a bid is too low or placed after auction closed. |
| `Auction.java` | Core Engine | Holds the shared state. Has `synchronized placeBid()`, `close()`, and `waitForResult()`. |
| `BidderBot.java` | `Runnable` Thread | Simulates an autonomous bidder that sleeps 100–500ms and tries to place bids. |
| `AuctionTimer.java` | `Runnable` Thread | Background countdown timer that closes the auction after N seconds. |
| `DBHelper.java` | Database Helper | All MySQL code in one place: connects, inserts items, batch-inserts bids, and queries history. |
| `Main.java` | Controller / Menu | Console menu interface, thread startup, and coordinating user actions. |

---

## 4. Setup on Your Local Laptop

### Prerequisites
- JDK 8 or higher (`java -version` and `javac -version`)
- MySQL Server installed and running

### Step-by-Step

1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd concurrent_bidding_system
   ```

2. **Initialize the MySQL database**:
   Run the SQL script using your MySQL root password:
   ```bash
   mysql -u root -p < schema.sql
   ```
   *(This creates the `auction_db` database and the `items` and `bids` tables).*

3. **Check the MySQL Connector JAR**:
   Ensure `mysql-connector-j-8.4.0.jar` is inside the `lib/` directory.

4. **Set your MySQL password**:
   Open `src/DBHelper.java` and make sure lines 11–13 match your MySQL credentials:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
   private static final String USER = "root";
   private static final String PASSWORD = "your_mysql_password";
   ```

5. **Compile and Run**:
   - **Linux / macOS**:
     ```bash
     javac -cp "lib/*:src" src/*.java
     java -cp "lib/*:src" Main
     ```
   - **Windows**:
     ```cmd
     javac -cp "lib/*;src" src/*.java
     java -cp "lib/*;src" Main
     ```

---

## 5. Suggested Viva Role Division

If your team has 2, 3, or 4 members, here is the best way to divide speaking topics for the presentation/viva:

### If 2 Members:
- **Member 1 (OOP & Database)**:
  - Explain Domain Models (`Item`, `Bid`, `InvalidBidException`).
  - Explain MySQL schema (`items`, `bids` with foreign key).
  - Explain `DBHelper` (`PreparedStatement`, batch execution, auto-generated keys).
- **Member 2 (Concurrency & Threads)**:
  - Explain Multi-threading (`BidderBot`, `AuctionTimer`, `Runnable` vs `Thread`).
  - Explain Thread Synchronization (`synchronized`, monitor lock, `wait()`, `notifyAll()`, `join()`).
  - Explain `HashMap` report for most active bidder.

### If 3 Members:
- **Member 1 (Architecture & Domain)**:
  - Problem statement, console menu in `Main.java`.
  - POJO classes (`Item`, `Bid`), `InvalidBidException`.
  - Collections: `ArrayList` for bid history and `HashMap` for bidder activity breakdown.
- **Member 2 (Multi-threading & Synchronization)**:
  - Why we need threads (`BidderBot` and `AuctionTimer` running together).
  - Why `synchronized` is used on `placeBid()`.
  - How `wait()`, `notifyAll()`, and `join()` coordinate the threads.
- **Member 3 (Database & JDBC)**:
  - MySQL database design (2 tables, primary key, foreign key).
  - Why `PreparedStatement` was used instead of `Statement` (SQL injection, speed).
  - JDBC batch operations (`addBatch()`, `executeBatch()`) and `try-with-resources`.

### If 4 Members:
- **Member 1**: Overview, Menu Flow, OOP Design (`Item`, `Bid`, `InvalidBidException`).
- **Member 2**: Threads implementation (`BidderBot`, `AuctionTimer`, `Runnable` vs `Thread`, simulated jitter).
- **Member 3**: Concurrency control (`synchronized` methods, monitor locks, `wait()`, `notifyAll()`, `join()`).
- **Member 4**: Database persistence (`schema.sql`, `DBHelper`, `PreparedStatement`, batch processing, `HashMap` report).

---

## 6. Top Viva Questions & Clear Answers

Here are the exact questions professors love to ask about this project:

### Q1: What is thread synchronization and why is `placeBid()` synchronized?
> **Answer**: `placeBid()` accesses shared mutable state (`highestBid` and `history`). If two bot threads call `placeBid()` at the exact same millisecond without `synchronized`, both could read the old highest bid, both pass the check, and both overwrite the highest bid. Marking it `synchronized` gives it mutual exclusion: only one thread can execute inside `placeBid()` at a time.

### Q2: Why did we implement `Runnable` instead of extending `Thread`?
> **Answer**:
> 1. Java supports single inheritance only. If a class extends `Thread`, it cannot extend any other class. Implementing `Runnable` keeps the class flexible.
> 2. Clean separation of concerns: `Runnable` defines the *task/job* (`run()`), while `Thread` is the *worker* that executes it.

### Q3: Why do we use `while (open) wait();` instead of `if (open) wait();` in `waitForResult()`?
> **Answer**:
> 1. **Spurious Wakeups**: A thread can occasionally wake up without `notify()` being called due to OS-level signaling quirks.
> 2. **State Re-verification**: Another thread might change the state between the moment a thread wakes up and the moment it re-acquires the lock. A `while` loop guarantees the condition is re-checked before proceeding.

### Q4: What is the purpose of `join()` in `Main.java`?
> **Answer**: `join()` tells the main thread to wait until the child thread (each bot and the timer) has completely finished executing its `run()` method before moving forward. This prevents the program from printing results or exiting while bots are still running.

### Q5: Why use `PreparedStatement` instead of `Statement`?
> **Answer**:
> 1. **Security**: Parameter placeholders (`?`) prevent SQL Injection attacks.
> 2. **Performance**: The SQL statement is compiled and optimized by the database once and reused for multiple executions.
> 3. **Type Safety**: Automatically handles data conversions (integers, strings, dates) without syntax errors from string concatenation.

### Q6: How is the most active bidder calculated?
> **Answer**: We iterate through the `history` `ArrayList<Bid>` and use a `HashMap<String, Integer>` where the key is the bidder's name and the value is their bid count. We update counts using `map.put(name, map.getOrDefault(name, 0) + 1)`, then find the key with the maximum value.
