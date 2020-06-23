public class Card {
    private String type;
    private String name;
    private String description;
    private int cost;
    private int value;
    private int numRemaining;
    private int numDurationPlays;
    private boolean trashCard;

    public Card() {
        type = "";
        name = "no card available";
        description = "";
        cost = 0;
        value = 0;
        numRemaining = 0;
        numDurationPlays = 0;
        trashCard = false;
    }

    public Card(String type, String name, String description, int cost, int value, int numRemaining, int numPlays) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.value = value;
        this.numRemaining = numRemaining;
        this.numDurationPlays = numPlays;
        trashCard = false;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getValue() {
        return value;
    }

    public int getNumRemaining() {
        return numRemaining;
    }

    public void decreaseNumRemaining() {
        --numRemaining;
    }

    public int getNumDurationPlays() {
        return numDurationPlays;
    }

    public void setNumDurationPlays(int numDurationPlays) {
        this.numDurationPlays = numDurationPlays;
    }

    public void increaseNumDurationPlays() {
        ++numDurationPlays;
    }

    public void decreaseNumDurationPlays() {
        --numDurationPlays;
    }

    public void setTrashCard(boolean trashCard) {
        this.trashCard = trashCard;
    }

    public boolean getTrashCard() {
        return trashCard;
    }

    public String toString() {
        String card = "";
        if (type.contains("Action") || name.equals("Harem") || name.equals("Hoard") || name.equals("Talisman")) {
            card += (name + " $" + cost + "\n" + description + " (" + numRemaining + " remaining)");
        } else {
            card += (name + " $" + cost + " (" + numRemaining + " remaining)");
        }

        return card;
    }

    public String cardInHand() {
        String card = "";
        card += (type + ": " + name + " - " + description);
        return card;
    }
}
