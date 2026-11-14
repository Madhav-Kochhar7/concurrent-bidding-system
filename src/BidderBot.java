import java.util.Random;

public class BidderBot implements Runnable {
    private String name;
    private Auction auction;
    private Random random = new Random();
    private int minSleepMs = 100;
    private int maxSleepMs = 500;
    private boolean quiet = false;

    public BidderBot(String name, Auction auction) {
        this.name = name;
        this.auction = auction;
    }

    public BidderBot(String name, Auction auction, int minSleepMs, int maxSleepMs, boolean quiet) {
        this.name = name;
        this.auction = auction;
        this.minSleepMs = minSleepMs;
        this.maxSleepMs = maxSleepMs;
        this.quiet = quiet;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        while (auction.isOpen()) {
            try {
                // Sleep for jitter delay
                int sleepRange = Math.max(1, maxSleepMs - minSleepMs + 1);
                int sleepTime = minSleepMs + random.nextInt(sleepRange);
                Thread.sleep(sleepTime);

                if (!auction.isOpen()) {
                    break;
                }

                // Bid a bit above the current minimum acceptable bid
                int min = auction.getMinimumNextBid();
                int extra = random.nextInt(auction.getItem().getMinIncrement() * 2 + 1);
                int bidAmount = min + extra;

                try {
                    auction.placeBid(name, bidAmount);
                    if (!quiet) {
                        System.out.println("[Bot] " + name + " placed bid: " + bidAmount);
                    }
                } catch (InvalidBidException e) {
                    // Ignored: another bot may have placed a higher bid first
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
