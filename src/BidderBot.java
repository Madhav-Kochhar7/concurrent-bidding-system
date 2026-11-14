import java.util.Random;

public class BidderBot implements Runnable {
    private String name;
    private Auction auction;
    private Random random = new Random();

    public BidderBot(String name, Auction auction) {
        this.name = name;
        this.auction = auction;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        while (auction.isOpen()) {
            try {
                // Sleep for random interval between 600ms and 1100ms (paced bidding)
                int sleepTime = 600 + random.nextInt(501);
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
                    System.out.println("[Bot] " + name + " placed bid: " + bidAmount);
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
