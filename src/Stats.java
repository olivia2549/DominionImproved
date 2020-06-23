public class Stats {
    private int money;
    private int actions;
    private int buys;

    public Stats() {
        money = 0;
        actions = 1;
        buys = 1;
    }

    public int getMoney() {
        return money;
    }

    public String changeMoney(int incrementNum) {
        money += incrementNum;
        return "Added +$" + incrementNum + ".";
    }

    public int getActions() {
        return actions;
    }

    public String changeActions(int incrementNum) {
        actions += incrementNum;
        return "Added " + incrementNum + " Action(s).";
    }

    public int getBuys() {
        return buys;
    }

    public String changeBuys(int incrementNum) {
        buys += incrementNum;
        return "Added " + incrementNum + " Buy(s).";
    }

    public void printStats(GameManager gameManager) {
        System.out.println("Money: " + money + "\nBuys: " + buys + "\nActions: " + actions);
        System.out.println("Available action cards:");
        boolean hasAction = false;
        for (Card card : gameManager.getHand()) {
            if (card.getType().contains("Action")) {
                System.out.println("\t" + card.cardInHand());
                hasAction = true;
            }
        }
        if (!hasAction) {
            System.out.println("\tNone");
        }

        System.out.println();
    }
}
