import java.util.ArrayList;

public class Auction {
    private Item item;
    private Bid highestBid;
    private ArrayList<Bid> history = new ArrayList<>();
    private boolean open = true;
    private boolean unsafeMode = false;

    public Auction(Item item) {
        this.item = item;
    }

    public void setUnsafeMode(boolean unsafeMode) {
        this.unsafeMode = unsafeMode;
    }

    public boolean isUnsafeMode() {
        return unsafeMode;
    }

    public void placeBid(String bidder, int amount) throws InvalidBidException {
        if (unsafeMode) {
            placeBidUnsafe(bidder, amount);
        } else {
            placeBidSafe(bidder, amount);
        }
    }

    // Safe version: synchronized prevents multiple threads from interleaving
    private synchronized void placeBidSafe(String bidder, int amount) throws InvalidBidException {
        if (!open) {
            throw new InvalidBidException("Auction is closed");
        }
        int minimum = (highestBid == null) ? item.getStartPrice()
                                           : highestBid.getAmount() + item.getMinIncrement();
        if (amount < minimum) {
            throw new InvalidBidException("Bid must be at least " + minimum);
        }
        highestBid = new Bid(bidder, amount);
        history.add(highestBid);
    }

    // Unsafe version: WITHOUT synchronized and with an artificial 1ms delay
    // between checking and saving, multiple threads will experience race conditions
    private void placeBidUnsafe(String bidder, int amount) throws InvalidBidException {
        if (!open) {
            throw new InvalidBidException("Auction is closed");
        }
        int minimum = (highestBid == null) ? item.getStartPrice()
                                           : highestBid.getAmount() + item.getMinIncrement();
        if (amount < minimum) {
            throw new InvalidBidException("Bid must be at least " + minimum);
        }

        try {
            // Artificial delay between checking and saving
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        highestBid = new Bid(bidder, amount);
        history.add(highestBid);
    }

    public synchronized void close() {
        if (!open) {
            return;
        }
        open = false;
        if (highestBid != null) {
            item.setWinner(highestBid.getBidder());
            item.setFinalPrice(highestBid.getAmount());
        }
        notifyAll();
    }

    public synchronized Bid waitForResult() throws InterruptedException {
        while (open) {
            wait();
        }
        return highestBid;
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public synchronized Item getItem() {
        return item;
    }

    public synchronized Bid getHighestBid() {
        return highestBid;
    }

    public synchronized ArrayList<Bid> getHistory() {
        return history;
    }

    public synchronized int getMinimumNextBid() {
        return (highestBid == null) ? item.getStartPrice()
                                   : highestBid.getAmount() + item.getMinIncrement();
    }
}
