import java.util.ArrayList;

public class Auction {
    private Item item;
    private Bid highestBid;
    private ArrayList<Bid> history = new ArrayList<>();
    private boolean open = true;

    public Auction(Item item) {
        this.item = item;
    }

    public synchronized void placeBid(String bidder, int amount) throws InvalidBidException {
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

    public synchronized void close() {
        open = false;
        if (highestBid != null) {
            item.setWinner(highestBid.getBidder());
            item.setFinalPrice(highestBid.getAmount());
        }
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
