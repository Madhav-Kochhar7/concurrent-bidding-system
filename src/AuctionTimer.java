public class AuctionTimer implements Runnable {
    private Auction auction;
    private int durationSeconds;

    public AuctionTimer(Auction auction, int durationSeconds) {
        this.auction = auction;
        this.durationSeconds = durationSeconds;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(durationSeconds * 1000L);
        } catch (InterruptedException e) {
            // Timer interrupted early
        }
        System.out.println("\n[Timer] Time's up (" + durationSeconds + "s)! Closing auction...");
        auction.close();
    }
}
