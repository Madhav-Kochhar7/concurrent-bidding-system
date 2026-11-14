import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("      Online Auction System      ");
        System.out.println("=================================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Create an item and start manual auction");
            System.out.println("2. Run auction on an item (with 2 bots, 5s)");
            System.out.println("3. Show all items and results");
            System.out.println("4. Show bid history of an item");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    runManualAuction();
                    break;
                case "2":
                    runBotAuction();
                    break;
                case "3":
                    showAllItemsFromDB();
                    break;
                case "4":
                    showBidHistoryFromDB();
                    break;
                case "0":
                    running = false;
                    System.out.println("Exiting. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1, 2, 3, 4, or 0.");
            }
        }
    }

    private static void runManualAuction() {
        System.out.println("\n--- Add New Item ---");
        System.out.print("Enter item name: ");
        String name = scanner.nextLine().trim();
        while (name.isEmpty()) {
            System.out.print("Name cannot be empty. Enter item name: ");
            name = scanner.nextLine().trim();
        }

        int startPrice = readPositiveInt("Enter starting price: ");
        int minIncrement = readPositiveInt("Enter minimum increment: ");

        Item item = new Item(name, startPrice, minIncrement);
        saveItemToDatabase(item);

        Auction auction = new Auction(item);

        System.out.println("\nAuction started for: " + item.getName());
        System.out.println("Starting price: " + item.getStartPrice());
        System.out.println("Minimum increment: " + item.getMinIncrement());
        System.out.println("(Type 'close' as bidder name to end the auction)");

        while (auction.isOpen()) {
            System.out.println("\nMinimum required next bid: " + auction.getMinimumNextBid());
            System.out.print("Enter bidder name (or 'close' to finish): ");
            String bidder = scanner.nextLine().trim();

            if (bidder.equalsIgnoreCase("close")) {
                auction.close();
                break;
            }

            if (bidder.isEmpty()) {
                System.out.println("Bidder name cannot be empty.");
                continue;
            }

            int amount = readPositiveInt("Enter bid amount: ");

            try {
                auction.placeBid(bidder, amount);
                System.out.println("SUCCESS: Bid accepted! " + bidder + " holds the highest bid at " + amount);
            } catch (InvalidBidException e) {
                System.out.println("REJECTED: " + e.getMessage());
            }
        }

        printAuctionSummary(auction);
        saveAuctionResultsToDatabase(auction);
    }

    private static void runBotAuction() {
        System.out.println("\n--- Add Item for Bot Auction ---");
        System.out.print("Enter item name: ");
        String name = scanner.nextLine().trim();
        while (name.isEmpty()) {
            System.out.print("Name cannot be empty. Enter item name: ");
            name = scanner.nextLine().trim();
        }

        int startPrice = readPositiveInt("Enter starting price: ");
        int minIncrement = readPositiveInt("Enter minimum increment: ");

        Item item = new Item(name, startPrice, minIncrement);
        saveItemToDatabase(item);

        Auction auction = new Auction(item);

        System.out.println("\nStarting auction for 5 seconds with 2 bots (Bot-1 and Bot-2)...");
        System.out.println("Item: " + item.getName() + " (Start: " + item.getStartPrice() + ", Min Increment: " + item.getMinIncrement() + ")");

        ArrayList<Thread> botThreads = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Thread bot = new Thread(new BidderBot("Bot-" + i, auction));
            botThreads.add(bot);
        }

        Thread timerThread = new Thread(new AuctionTimer(auction, 5));

        // Start all threads
        for (Thread bot : botThreads) {
            bot.start();
        }
        timerThread.start();

        // Main thread waits for the auction to finish
        try {
            auction.waitForResult();

            // Wait for timer and bots to finish
            timerThread.join();
            for (Thread bot : botThreads) {
                bot.join();
            }

            printAuctionSummary(auction);
            saveAuctionResultsToDatabase(auction);
        } catch (InterruptedException e) {
            System.out.println("Auction was interrupted: " + e.getMessage());
        }
    }

    private static void showAllItemsFromDB() {
        System.out.println("\n--- All Items and Results (from Database) ---");
        try {
            ArrayList<Item> items = DBHelper.getAllItems();
            if (items.isEmpty()) {
                System.out.println("No items found in database.");
                return;
            }
            System.out.printf("%-5s %-20s %-12s %-15s %-15s %-12s%n",
                "ID", "Name", "Start Price", "Min Increment", "Winner", "Final Price");
            System.out.println("----------------------------------------------------------------------------------");
            for (Item item : items) {
                String winner = (item.getWinner() != null) ? item.getWinner() : "(No winner / Unsold)";
                String finalPrice = (item.getFinalPrice() > 0) ? String.valueOf(item.getFinalPrice()) : "N/A";
                System.out.printf("%-5d %-20s %-12d %-15d %-15s %-12s%n",
                    item.getId(),
                    item.getName(),
                    item.getStartPrice(),
                    item.getMinIncrement(),
                    winner,
                    finalPrice);
            }
        } catch (SQLException e) {
            System.out.println("[DB Error] Could not retrieve items: " + e.getMessage());
        }
    }

    private static void showBidHistoryFromDB() {
        System.out.println("\n--- Show Bid History (from Database) ---");
        int itemId = readPositiveInt("Enter item ID: ");
        try {
            Item item = DBHelper.getItemById(itemId);
            if (item == null) {
                System.out.println("No item found with ID " + itemId + " in database.");
                return;
            }
            System.out.println("Item: " + item.getName() + " (Start: " + item.getStartPrice() + ", Min Increment: " + item.getMinIncrement() + ")");
            ArrayList<String> history = DBHelper.getBidHistory(itemId);
            if (history.isEmpty()) {
                System.out.println("No bids recorded for this item.");
            } else {
                System.out.println("Bid History (" + history.size() + " bids):");
                for (String record : history) {
                    System.out.println("  " + record);
                }
            }
        } catch (SQLException e) {
            System.out.println("[DB Error] Could not retrieve bid history: " + e.getMessage());
        }
    }

    private static void saveItemToDatabase(Item item) {
        try {
            int id = DBHelper.saveItem(item);
            if (id > 0) {
                System.out.println("[DB] Item saved to database with ID: " + id);
            }
        } catch (SQLException e) {
            System.out.println("[DB Notice] Could not save item to MySQL: " + e.getMessage());
        }
    }

    private static void saveAuctionResultsToDatabase(Auction auction) {
        Item item = auction.getItem();
        if (item.getId() > 0) {
            try {
                DBHelper.saveBids(item.getId(), auction.getHistory());
                DBHelper.saveResult(item.getId(), item.getWinner(), item.getFinalPrice());
                System.out.println("[DB] Auction result and " + auction.getHistory().size() + " bids saved to database.");
            } catch (SQLException e) {
                System.out.println("[DB Notice] Could not save bids/result to MySQL: " + e.getMessage());
            }
        }
    }

    private static void printAuctionSummary(Auction auction) {
        System.out.println("\n=================================");
        System.out.println("         AUCTION CLOSED          ");
        System.out.println("=================================");
        Bid highest = auction.getHighestBid();
        if (highest != null) {
            System.out.println("Item        : " + auction.getItem().getName());
            System.out.println("Winner      : " + highest.getBidder());
            System.out.println("Final Price : " + highest.getAmount());
        } else {
            System.out.println("Item   : " + auction.getItem().getName());
            System.out.println("Result : UNSOLD (no valid bids placed)");
        }

        System.out.println("Total bids placed: " + auction.getHistory().size());
        if (!auction.getHistory().isEmpty()) {
            System.out.println("Bid History:");
            for (int i = 0; i < auction.getHistory().size(); i++) {
                System.out.println("  " + (i + 1) + ". " + auction.getHistory().get(i));
            }
            printMostActiveBidderReport(auction.getHistory());
        }
    }

    private static void printMostActiveBidderReport(ArrayList<Bid> history) {
        if (history.isEmpty()) {
            return;
        }

        // Using HashMap<String, Integer> to count bids per bidder
        HashMap<String, Integer> bidCounts = new HashMap<>();
        for (Bid bid : history) {
            String bidder = bid.getBidder();
            bidCounts.put(bidder, bidCounts.getOrDefault(bidder, 0) + 1);
        }

        String mostActiveBidder = null;
        int maxBids = 0;
        for (HashMap.Entry<String, Integer> entry : bidCounts.entrySet()) {
            if (entry.getValue() > maxBids) {
                maxBids = entry.getValue();
                mostActiveBidder = entry.getKey();
            }
        }

        System.out.println("\n--- Bidder Activity Report (HashMap) ---");
        System.out.println("Most Active Bidder: " + mostActiveBidder + " (" + maxBids + " bids)");
        System.out.println("Breakdown by Bidder:");
        for (HashMap.Entry<String, Integer> entry : bidCounts.entrySet()) {
            System.out.println("  - " + entry.getKey() + ": " + entry.getValue() + " bid(s)");
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value > 0) {
                    return value;
                }
                System.out.println("Please enter a positive number greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid whole number.");
            }
        }
    }
}
