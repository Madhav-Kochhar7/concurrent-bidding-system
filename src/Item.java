public class Item {
    private int id;
    private String name;
    private int startPrice;
    private int minIncrement;
    private String winner;
    private int finalPrice;

    public Item(String name, int startPrice, int minIncrement) {
        this.name = name;
        this.startPrice = startPrice;
        this.minIncrement = minIncrement;
    }

    public Item(int id, String name, int startPrice, int minIncrement) {
        this.id = id;
        this.name = name;
        this.startPrice = startPrice;
        this.minIncrement = minIncrement;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getStartPrice() {
        return startPrice;
    }

    public int getMinIncrement() {
        return minIncrement;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(int finalPrice) {
        this.finalPrice = finalPrice;
    }

    @Override
    public String toString() {
        if (id > 0) {
            return "Item #" + id + ": " + name + " (Start Price: " + startPrice + ", Min Increment: " + minIncrement + ")";
        }
        return name + " (Start Price: " + startPrice + ", Min Increment: " + minIncrement + ")";
    }
}
