import java.util.*;

public class GameManager {
    private Dominion dominion;
    private ArrayList<Card> allCards;
    private ArrayList<Card> kingdomCards;
    private ArrayList<Card> drawPile;
    private ArrayList<Card> discardPile;
    private ArrayList<Card> cardsInMiddle;
    private ArrayList<Card> hand;
    private ArrayList<Card> actionsPlayed;
    private int pointTokens;
    private Stats stats;

    public GameManager() {
        allCards = new ArrayList<>();
        kingdomCards = new ArrayList<>();
        drawPile = new ArrayList<>();
        discardPile = new ArrayList<>();
        cardsInMiddle = new ArrayList<>();
        hand = new ArrayList<>();
        actionsPlayed = new ArrayList<>();
        pointTokens = 0;
        stats = new Stats();
        dominion = new Dominion();
    }


    // ************************************************************************************* POINTS
    public void increasePointTokens(int incrementNum) {
        pointTokens += incrementNum;
    }

    public void boughtEndingVictory(boolean usingProsperity, boolean opponent) {
        String name;
        if (usingProsperity) {
            name = "Colony";
        } else {
            name = "Province";
        }

        // If opponent is the one who bought it, manually decrease num remaining
        if (opponent) {
            findCard(name, cardsInMiddle).decreaseNumRemaining();
        }
        System.out.print(name + "s remaining: ");
        System.out.println(findCard(name, cardsInMiddle).getNumRemaining());
        System.out.println();
    }

    public int calculateEndPoints(boolean usingProsperity) {
        int endPoints = pointTokens;
        int numCards = 0;
        int numGardens = 0;
        int numEstates = 0;
        int numDuchys = 0;
        int numProvinces = 0;
        int numColonies = 0;
        int numOtherVictoryCards = 0;
        int numCurses = 0;

        // Put all the cards into one pile and count up the points
        discardPile.addAll(drawPile);
        for (Card card : discardPile) {
            ++numCards;
            if (card.getType().contains("Victory") || card.getType().equals("Curse")) {
                endPoints += card.getValue();
                switch (card.getName()) {
                    case "Estate":
                        numEstates += 1;
                        break;
                    case "Duchy":
                        numDuchys += 1;
                        break;
                    case "Province":
                        numProvinces += 1;
                        break;
                    case "Colony":
                        numColonies += 1;
                        break;
                    case "Curse":
                        numCurses += 1;
                        break;
                    case "Harem":
                    case "Mill":
                        numOtherVictoryCards += 1;
                        break;
                }
            } else if (card.getName().equals("Gardens")) {
                numGardens += 1;
            }
        }
        int numPointsPerGarden = discardPile.size()/10;
        endPoints += numPointsPerGarden*numGardens;

        System.out.print("You ended with " +
                numCards + " total cards, " +
                numEstates + " estates, " +
                numDuchys + " duchys, " +
                numProvinces + " provinces, ");
        if (usingProsperity) {
            System.out.print(numColonies + " colonies, ");
        }
        System.out.println(numOtherVictoryCards + " other victory card(s), " +
                pointTokens + " victory point tokens, " +
                numGardens + " gardens, and " +
                numCurses + " curses.");

        return endPoints;
    }


    // ************************************************************************** SETUP/PRINT STUFF
    public void addToAllCards(Card card) {
        allCards.add(card);
    }

    public void addToCardsInMiddle(String name) {
        Card card = findCard(name, allCards);
        cardsInMiddle.add(card);
    }

    public void addToCardsInMiddle(int setNum, List<ArrayList<String>> dominionSets) {
        for (String name : dominionSets.get(setNum-1)) {
            Card card = findCard(name, allCards);
            cardsInMiddle.add(card);
        }
    }

    public void printKingdomCards() {
        System.out.println("ALL KINGDOM CARDS");
        for (Card card : allCards) {
            if (card.getType().contains("Action") ||
                    card.getName().equals("Harem") ||
                    card.getName().equals("Hoard") ||
                    card.getName().equals("Talisman")) {
                System.out.println(card + "\n");
                kingdomCards.add(card);
            }
        }
    }

    public void printSet(String title, ArrayList<String> dominionSets) {
        System.out.print(title);
        for (int i = 0; i < dominionSets.size(); ++i) {
            if (i != 9) {
                System.out.print(dominionSets.get(i) + ", ");
            } else {
                System.out.println(dominionSets.get(i));
            }
        }
    }

    public void generateRandom() {
        Random rand = new Random();
        rand.setSeed(2797);

        int randNum = rand.nextInt(kingdomCards.size());
        for (int i=0; i<10; ++i) {
            String name = kingdomCards.get(randNum).getName();
            while (notValid(name)) {
                randNum = rand.nextInt(kingdomCards.size());
                name = kingdomCards.get(randNum).getName();
            }
            cardsInMiddle.add(findCard(kingdomCards.get(randNum).getName(), allCards));
            randNum = rand.nextInt(kingdomCards.size());
        }
    }

    public void printCardsInMiddle() {
        System.out.println("\nAvailable decks:");
        for (int i = 0; i < 9; ++i) {
            System.out.print(cardsInMiddle.get(i).getName() + ", ");
        }
        System.out.println(cardsInMiddle.get(9).getName() + "\n");
    }

    public void printMenuOptions(boolean usingProsperity) {
        System.out.println("------------- MENU OPTIONS -------------");
        if (usingProsperity) {
            System.out.println("1. Opponent bought a Colony");
        } else {
            System.out.println("1. Opponent bought a Province");
        }
        System.out.println("2. Opponent played attack/benefit card affecting me");
        System.out.println("3. Play action");
        System.out.println("4. Buy card");
        System.out.println("5. End turn");

        System.out.println();
        System.out.println("Enter your choice:");
    }

    public void printStats() {
        stats.printStats(this);
    }

    public void printAttack(Scanner scnr) {
        System.out.println("If your opponent does not have a reaction card, they must " +
                "now choose option 2 of their menu to be affected by your attack.");
        System.out.println("Press enter when they have done so:");
        scnr.nextLine();
    }


    // ********************************************************************************* RESET TURN
    public void reset() {
        discardPile.addAll(hand);
        for (Card card : actionsPlayed) {
            if (!card.getTrashCard()) {
                discardPile.add(card);
            }
        }
        actionsPlayed.clear();
        hand.clear();
        stats = new Stats();
    }

    public boolean isGameOver() {
        int numEmptyStacks = 0;
        for (Card card : cardsInMiddle) {
            if ((card.getNumRemaining() == 0) && (card.getType().contains("Action"))) {
                numEmptyStacks += 1;
            }
        }
        return numEmptyStacks >= 3;
    }


    // ********************************************************************************** DRAW PILE
    public void addToDrawPile(String name) {
        Card card = findCard(name);
        drawPile.add(card);
    }

    public void shuffleDrawPile() {
        Collections.shuffle(drawPile);
    }

    public void drawNewHand() {
        System.out.println("********************** Your next hand of cards **********************");
        for (int i = 0; i < 5; ++i) {
            drawCard(false);
            System.out.println(hand.get(i).cardInHand());
        }
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public Card drawCard(boolean printCard) {
        // If draw pile has <1 card, shuffle discard pile and add it to bottom
        if (drawPile.size() < 1) {
            if (discardPile.size() > 0) {
                Collections.shuffle(discardPile);
                drawPile.addAll(discardPile);
                discardPile.clear();
            } else {
                System.out.println("No cards left to draw.\n");
                return new Card();
            }
        }

        Card card = drawPile.get(0);
        hand.add(card);
        if (printCard) {
            System.out.println("You drew the " + card.getName() + " card.");
        }

        if (card.getType().contains("Treasure")) {
            stats.changeMoney(card.getValue());
        }

        drawPile.remove(0);
        return card;
    }


    // ******************************************************************************* DISCARD PILE
    public void discardCard(ArrayList<Card> list, Card cardToDiscard, boolean hand) {
        System.out.println("Discarding the " + cardToDiscard.getName() + " card...");
        discardPile.add(cardToDiscard);
        if (cardToDiscard.getType().contains("Treasure") && hand) {
            stats.changeMoney(cardToDiscard.getValue()*-1);
        }
        list.remove(cardToDiscard);
        System.out.println("Done.\n");
    }


    // *************************************************************************************** GAIN
    public void gainCard(Scanner scnr, Card cardToGain) {
        if (cardToGain.getNumRemaining() > 0) {
            System.out.println("Gaining the " + cardToGain.getName() + " card...");
            discardPile.add(cardToGain);
            cardToGain.decreaseNumRemaining();
            System.out.println("Done.\n");
            specialGains(scnr, cardToGain);
        } else {
            System.out.println("The " + cardToGain.getName() + " supply is out of cards.\n");
        }
    }

    /**
     *
     * Certain cards have special effects when a card is gained, so this checks for that
     */
    public void specialGains(Scanner scnr, Card cardGained) {
        checkWatchtower(scnr, cardGained);
    }

    public void checkWatchtower(Scanner scnr, Card cardGained) {
        boolean hasWatchtower = hand.contains(findCard("Watchtower", hand));

        if (hasWatchtower) {
            discardPile.remove(0);
            System.out.println("You have the watchtower in hand. Would you like to (1) trash the "
                    + cardGained.getName() + " card or (2) put it on your deck?");
            String optionStr = scnr.nextLine();
            int choice = getValidDigit(scnr, optionStr, 2);
            if (choice == 1) {
                System.out.println("Trashing the " + cardGained.getName() + " card...");
                System.out.println("Done.\n");
            }
            if (choice == 2) {
                System.out.println("Adding the " + cardGained.getName() + " card to your draw pile...");
                drawPile.add(0, cardGained);
                System.out.println("Done.\n");
            }
        }
    }


    // **************************************************************************************** BUY
    /**
     *
     * Choose a card to buy from the options available based on money
     * @return number corresponding to the user's choice of card to buy
     */
    public int chooseBuy(Scanner scnr, ArrayList<Card> options) {
        int option = 1;

        System.out.println("Your options:");
        // Make sure the options are only cards the user can afford
        for (Card card : cardsInMiddle) {
            if ((card.getCost() <= stats.getMoney()) && card.getNumRemaining() > 0) {
                System.out.println(option + ". " + card + "\n");
                options.add(card);
                ++option;
            }
        }
        System.out.println((options.size() + 1) + ". Cancel");

        System.out.println();
        System.out.println("Choose an option to buy:");
        String optionStr = scnr.nextLine();
        return getValidDigit(scnr, optionStr, option);
    }

    /**
     *
     * Allows user to buy a card
     * @return whether a card was actually bought or not (determines whether stats should be reprinted)
     */
    public boolean buyCard(Scanner scnr, boolean usingProsperity) {

        if (stats.getBuys() == 0) {
            System.out.println("Sorry, you have no more buys left. Cannot execute option.\n");
            System.out.println("Please enter a different choice:");
            return true;
        } else {
            subtractPrices();   // Some action cards subtract pricing of other cards

            // Choose a card to buy
            ArrayList<Card> options = new ArrayList<>();
            int choice = chooseBuy(scnr, options);

            // Buy the card
            System.out.println();
            if (choice != options.size() + 1) {
                Card cardBought = options.get(choice - 1);
                gainCard(scnr, cardBought);

                // Make special things happen when certain cards are bought
                specialBuys(cardBought, scnr, usingProsperity);

                // Decrease the buys and money left for this turn
                stats.changeBuys(-1);
                stats.changeMoney(cardBought.getCost()*-1);
            } else {
                System.out.println("Buy cancelled.\n");
            }

            addPrices();    // Put prices back to normal

            System.out.println();
            printStats();
            return false;
        }
    }

    /**
     *
     * Makes extra things happen when certain cards are bought
     */
    public void specialBuys(Card cardBought, Scanner scnr, boolean usingProsperity) {
        checkBorderVillage(scnr, cardBought);
        checkGoons();
        checkHoard(scnr, cardBought);
        checkEndingVictory(cardBought, usingProsperity);
        checkTalisman(scnr, cardBought);
    }

    public void checkBorderVillage(Scanner scnr, Card cardGained) {
        if (cardGained.getName().equals("Border Village")) {
            System.out.println("You may also gain a cheaper card.");
            Card borderVillageGain = printOptions(scnr, cardsInMiddle, "Border Village", 5);
            gainCard(scnr, borderVillageGain);
        }
    }

    public void checkGoons() {
        int numGoons = Collections.frequency(actionsPlayed, findCard("Goons", actionsPlayed));
        increasePointTokens(numGoons);

        if (numGoons > 0) {
            System.out.println("You have " + numGoons + " Goons in play. Gaining " + numGoons
                    + " victory point token(s)...");
            System.out.println("Done.\n");
        }
    }

    public void checkHoard(Scanner scnr, Card cardBought) {
        boolean hasHoard = hand.contains(findCard("Hoard", hand));

        if (cardBought.getType().contains("Victory") && hasHoard) {
            System.out.println("You have a Hoard in play. Gaining a gold...");
            gainCard(scnr, findCard("Gold"));
            System.out.println("Done.\n");
        }
    }

    public void checkEndingVictory(Card cardBought, boolean usingProsperity) {
        String name;
        if (usingProsperity) {
            name = "Colony";
        } else {
            name = "Province";
        }
        if (cardBought.getName().equals(name)) {
            boughtEndingVictory(usingProsperity, false);
        }
    }

    public void checkTalisman(Scanner scnr, Card cardBought) {
        boolean hasTalisman = hand.contains(findCard("Talisman", hand));

        if (!cardBought.getType().contains("Victory") && cardBought.getCost() <= 4 && hasTalisman) {
            System.out.println("You have a Talisman in play. Gaining a copy of the " +
                    cardBought.getName() + " card...");
            gainCard(scnr, findCard(cardBought.getName()));
        }
    }

    public void subtractPrices() {
        manageBridgePrice(true);
        managePeddlerPrice(true);
    }

    public void addPrices() {
        manageBridgePrice(false);
        managePeddlerPrice(false);
    }

    public void managePeddlerPrice(boolean subtract) {
        if (subtract) {
            Card peddler = findCard("Peddler", cardsInMiddle);
            peddler.setCost(8 - (actionsPlayed.size()*2));
            if (peddler.getCost() < 0) {
                peddler.setCost(0);
            }
        } else {
            findCard("Peddler", cardsInMiddle).setCost(8);
        }
    }

    public void manageBridgePrice(boolean subtract) {
        int numBridges = Collections.frequency(actionsPlayed, findCard("Bridge", actionsPlayed));

        for (int i=0; i<numBridges; ++i) {
            if (subtract) {
                for (Card card : cardsInMiddle) {
                    card.setCost(card.getCost() - 1);
                    if (card.getCost() < 0) {
                        card.setCost(0);
                    }
                }
            } else {
                for (Card card : cardsInMiddle) {
                    card.setCost(card.getCost() + 1);
                }
            }
        }
    }


    // ************************************************************************************** TRASH
    public void trashCard(ArrayList<Card> list, Card cardToTrash, boolean hand) {
        System.out.println("Trashing the " + cardToTrash.getName() + " card...");

        if (cardToTrash.getType().contains("Treasure") && hand) {
            stats.changeMoney(cardToTrash.getValue()*-1);
        }

        list.remove(cardToTrash);
        System.out.println("Done.\n");
    }

    public void trashMultiple(Scanner scnr, int numToTrash) {
        ArrayList<Card> options = new ArrayList<>();

        int option = 1;
        for (Card card : hand) {
            System.out.println(option + ". " + card);
            options.add(card);
            ++option;
        }

        System.out.println();

        System.out.println("Choose the cards you would like to trash, each separated by a space:");
        String choices = scnr.nextLine();
        Scanner scanChoices = new Scanner(choices);

        for (int i=0; i<numToTrash; ++i) {
            if (scanChoices.hasNextInt()) {
                option = scanChoices.nextInt();
                System.out.println();
                Card chosenCard = options.get(option - 1);
                trashCard(hand, chosenCard, true);
            }
        }

        if (scanChoices.hasNextInt()) {
            System.out.println("\nLimit has been reached. Could not trash any more cards.\n");
        }
    }


    // ************************************************************************* HELPFUL ALGORITHMS
    public Card findCard(String name, ArrayList<Card> list) {
        Card card = new Card();
        for (Card value : list) {
            if (value.getName().equals(name)) {
                card = value;
            }
        }
        return card;
    }

    public Card findCard(String name) {
        return findCard(name, cardsInMiddle);
    }

    /**
     * Validates the cards being input by the user in the custom setting
     * @return !isValid
     */
    public boolean notValid(String name) {
        boolean isValid = false;
        for (Card card : allCards) {
            if (card.getName().equals(name)) {
                isValid = true;
                for (Card cardMid : cardsInMiddle) {
                    if (cardMid != null && cardMid.getName().equals(name)) {
                        isValid = false;
                        break;
                    }
                }
            }
        }

        return !isValid;
    }

    public int getValidDigit(Scanner scnr, String someString, int range) {
        Scanner checkScan = new Scanner(someString);
        int validDigit = 0;

        // If it's an integer, store it in validDigit
        if (checkScan.hasNextInt()) {
            validDigit = checkScan.nextInt();
        }

        // Re-prompt for input until validatedGuess is an int in the correct range
        while ((validDigit < 1) || (validDigit > range)) {
            System.out.println("Your choice must be in the range 1-" + range + ". Try again.");
            System.out.println("Please choose again:");
            someString = scnr.nextLine();
            checkScan = new Scanner(someString);
            if (checkScan.hasNextInt()) {
                validDigit = checkScan.nextInt();
            }
        }

        return validDigit;
    }

    /**
     *
     * Algorithm for printing a user's option
     *  (this function is called pretty much whenever the user has to choose something)
     * The condition for the options to be printed depends on the card in question
     * @return the card chosen (or a default card if no options were available)
     */
    public Card printOptions(Scanner scnr, ArrayList<Card> arrayList, String cardName, int cost) {
        int option = 1;
        ArrayList<Card> options = new ArrayList<>();

        System.out.println("Your options:");
        for (Card card : arrayList) {
            boolean condition = true;   // assume true, meaning you'd print every card in the list

            switch (cardName) {
                case "Border Village":
                    condition = card.getCost() <= cost;
                    break;
                case "Feast":
                    condition = card.getCost() <= 5;
                    break;
                case "Mine":
                    condition = card.getType().contains("Treasure") && !card.getName().equals("Gold");
                    break;
                case "Remake-Gain":
                    condition = (card.getCost() == (1 + cost));
                    break;
                case "Remodel-Gain":
                    condition = card.getCost() <= (2 + cost);
                    break;
                case "Throne Room/King's Court":
                    condition = card.getType().contains("Action") &&
                            !card.getName().equals("Throne Room") &&
                            !card.getName().equals("King's Court");
                    break;
                case "Workshop":
                    condition = card.getCost() <= 4;
                    break;
            }

            // Print the correct options
            if (condition) {
                System.out.println(option + ". " + card.getName());
                options.add(card);
                ++option;
            }
        }

        if (options.size() > 0) {
            System.out.println("\nEnter your choice:");
            String optionStr = scnr.nextLine();
            int choice = getValidDigit(scnr, optionStr, (option - 1));
            return options.get(choice - 1);
        }

        return new Card();
    }


    // ************************************************************************************ ACTIONS
    public Card chooseAction(Scanner scnr) {
        if (stats.getActions() == 0) {
            System.out.println("\nSorry, you have no more actions left. Cannot execute option.");
            System.out.println("Please enter a different choice:\n");
        } else {
            int option = 1;
            ArrayList<Card> options = new ArrayList<>();

            System.out.println();
            boolean hasAction = false;
            System.out.println("Your options:");
            for (Card card : hand) {
                if (card.getType().contains("Action")) {
                    System.out.println(option + ". " + card.getName());
                    options.add(card);
                    hasAction = true;
                    ++option;
                }
            }
            System.out.println((options.size() + 1) + ". Cancel\n");

            Card cardPlayed = new Card();
            if (hasAction) {
                System.out.println("Choose an action to play:");
                String optionStr = scnr.nextLine();
                int choice = getValidDigit(scnr, optionStr, option);
                if (choice != (options.size() + 1)) {
                    cardPlayed = options.get(choice - 1);
                    System.out.println("\nPlaying the " + cardPlayed.getName() + " card...");
                    hand.remove(cardPlayed);
                    stats.changeActions(-1);
                    if (!cardPlayed.getName().equals("Feast")) {
                        actionsPlayed.add(cardPlayed);
                    }
                    cardPlayed = dominion.playAction(scnr, cardPlayed, this);
                    cardPlayed.increaseNumDurationPlays();
                } else {
                    System.out.println("Action cancelled.");
                }
                System.out.println();
                printStats();
            } else {
                System.out.println("Sorry, you don't have any action cards. Cannot execute option.");
                System.out.println("Please enter a different choice:");
            }

            return cardPlayed;
        }

        return new Card();
    }

    public void adventurer() {
        for (int i=0; i<2; ++i) {
            Card card = drawCard(true);

            while (!card.getType().contains("Treasure")) {
                System.out.println("Not a treasure card. Discarding and drawing another...");
                discardCard(hand, card, true);
                card = drawCard(true);
            }
            System.out.println();
        }
    }

    public void ambassador(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("Choose a card from your hand to return to the Supply.");
            Card cardToReturn = printOptions(scnr, hand, "Ambassador", 0);

            int numCopies = Collections.frequency(hand, findCard(cardToReturn.getName(), hand));

            int choice = 2;
            if (numCopies > 1) {
                System.out.println("You have more than 1 copy of this card. Would you like to " +
                        "return them both? (1) yes, (2) no:");
                String optionStr = scnr.nextLine();
                choice = getValidDigit(scnr, optionStr, 2);
            }

            trashCard(hand, cardToReturn, true);
            if (choice == 1) {  // Remove the card again if they wanted both copies trashed.
                trashCard(hand, cardToReturn, true);
            }

            printAttack(scnr);
        } else {
            System.out.println("How many cards is your opponent giving to you? Enter 1 or 2:");
            String optionStr = scnr.nextLine();
            int choice = getValidDigit(scnr, optionStr, 2);

            System.out.println("Enter the name of the card your opponent said to gain:");
            String name = scnr.nextLine();

            // Gain the card(s) (with data validation)
            boolean isValid = false;
            for (Card card : cardsInMiddle) {
                if (card.getName().equals(name)) {
                    isValid = true;
                    gainCard(scnr, card);
                    if (choice == 2) {
                        gainCard(scnr, card);
                    }
                    break;
                }
            }
            while (!isValid) {
                System.out.println("Not a valid card. Try again:");
                name = scnr.nextLine();
                isValid = false;
                for (Card card : cardsInMiddle) {
                    if (card.getName().equals(name)) {
                        isValid = true;
                        gainCard(scnr, card);
                        if (choice == 2) {
                            gainCard(scnr, card);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void bandit(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("Gaining a gold...");
            discardPile.add(findCard("Gold", cardsInMiddle));
            System.out.println("Done.\n");
            printAttack(scnr);
        } else {
            if (drawPile.size() < 2) {  // Make sure there are enough cards to draw
                Collections.shuffle(discardPile);
                drawPile.addAll(discardPile);
                discardPile.clear();
            }
            Card topCard = drawPile.get(0);
            Card secondCard = drawPile.get(1);
            System.out.println("The top 2 cards of your deck are: " + topCard.getName() + " and " + secondCard.getName() + ".");
            boolean topCardTreasure = (topCard.getType().contains("Treasure") && !topCard.getName().equals("Copper"));
            boolean nextCardTreasure = (secondCard.getType().contains("Treasure") && !secondCard.getName().equals(
                    "Copper"));
            if (topCardTreasure && !nextCardTreasure) {
                trashCard(drawPile, topCard, false);
                discardCard(drawPile, secondCard, false);
            } else if (!topCardTreasure && nextCardTreasure) {
                trashCard(drawPile, secondCard, false);
                discardCard(drawPile, topCard, false);
            } else if (topCardTreasure) {
                System.out.println("You have 2 treasure cards. Which would you prefer to trash?");
                System.out.println("1. " + topCard.getName() + "\n2. " + secondCard.getName());
                String optionStr = scnr.nextLine();
                int choice = getValidDigit(scnr, optionStr, 2);
                if (choice == 1) {
                    trashCard(drawPile, topCard, false);
                    discardCard(drawPile, secondCard, false);
                } else {
                    trashCard(drawPile, secondCard, false);
                    discardCard(drawPile, topCard, false);
                }
            } else {
                System.out.println("Neither is a treasure card other than copper. Discarding them both...");
                discardCard(drawPile, topCard, false);
                discardCard(drawPile, secondCard, false);
            }
            System.out.println("Done.");
        }
    }

    public void baron(Scanner scnr) {
        System.out.println(stats.changeBuys(1));

        boolean discardedEstate = false;
        for (Card card : hand) {
            if (card.getName().equals("Estate")) {
                System.out.println("Would you like to discard an estate for +$4? (1) yes, (2) no:");
                String choiceStr = scnr.nextLine();
                int choice = getValidDigit(scnr, choiceStr, 2);
                if (choice == 1) {
                    discardedEstate = true;
                    discardCard(hand, card, true);
                    System.out.println(stats.changeMoney(4));
                }
                break;
            }
        }

        if (!discardedEstate) {
            System.out.println("No Estate discarded. Gaining an Estate...");
            gainCard(scnr, findCard("Estate", cardsInMiddle));
            System.out.println("Done.\n");
        }
    }

    public void bishop(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("Gaining +$1...");
            stats.changeMoney(1);
        }

        boolean trashCard = true;
        if (opponent) {
            System.out.println("Would you like to trash a card from your hand? (1) yes, (2) no:");
            String optionStr = scnr.nextLine();
            int choice = getValidDigit(scnr, optionStr, 2);
            if (choice == 2) {
                trashCard = false;
            }
        }

        if (trashCard) {
            System.out.println("Choose a card from your hand to trash.");
            Card cardToTrash = printOptions(scnr, hand, "Bishop", 0);
            trashCard(hand, cardToTrash, true);

            if (!opponent) {
                int pointsToGain = cardToTrash.getCost()/2;
                System.out.println("Gaining " + pointsToGain + " point token(s) plus 1 free point token\n");
                increasePointTokens(pointsToGain + 1);

                System.out.println("Your opponent(s) may trash a card.");
                System.out.println("Press enter when they have done so:");
                scnr.nextLine();
            }
        }
    }

    public void bridge() {
        System.out.println(stats.changeBuys(1));
        System.out.println(stats.changeMoney(1));
    }

    public void borderVillage() {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(2));
    }

    public void bureaucrat(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("Adding a silver card to your deck...");
            drawPile.add(0, findCard("Silver", cardsInMiddle));
            findCard("Silver", cardsInMiddle).decreaseNumRemaining();
            System.out.println("Done.\n");
            printAttack(scnr);
        } else {
            boolean hasVictory = false;
            for (Card card : hand) {
                if (card.getType().contains("Victory")) {
                    System.out.println("\nPlacing the " + card.getName() + " card on your deck...");
                    drawPile.add(0, card);
                    hand.remove(card);
                    System.out.println("Done.");
                    hasVictory = true;
                    break;
                }
            }
            if (!hasVictory) {
                System.out.println("You have no victory cards. No cards added to your draw pile.");
            }
        }
    }

    public void cellar(Scanner scnr) {

        System.out.println(stats.changeActions(1));

        ArrayList<Card> options = new ArrayList<>();

        int option = 1;
        for (Card card : hand) {
            System.out.println(option + ". " + card);
            options.add(card);
            ++option;
        }

        System.out.println();

        System.out.println("Choose the number(s) of the card(s) you would like to discard, each separated by a space:");
        String choices = scnr.nextLine();
        Scanner scanChoices = new Scanner(choices);

        while (scanChoices.hasNextInt()) {
            option = scanChoices.nextInt();
            System.out.println();
            Card chosenCard = options.get(option - 1);
            discardCard(hand, chosenCard, true);

            System.out.println("Drawing a replacement...");
            drawCard(true);
        }

    }

    public void chancellor() {
        System.out.println(stats.changeMoney(2));
        System.out.println("Moving the draw pile to the discard pile...");
        while (drawPile.size() > 0) {
            discardPile.add(drawPile.get(0));
            drawPile.remove(0);
        }
        System.out.println("Done.");
    }

    public void chapel(Scanner scnr) {
        trashMultiple(scnr, 4);
    }

    public void councilRoom(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("+1 buy:");
            stats.changeBuys(1);
            printStats();

            System.out.println("Drawing 4 cards...");
            for (int i=0; i<4; ++i) {
                drawCard(true);
            }

            System.out.println();
            System.out.println("Your opponent must now choose option 2 of their menu to draw a card.");
            System.out.println("Press enter when they have done so:");
            scnr.nextLine();
        } else {
            System.out.println("\nDrawing a card...");
            drawCard(true);
            printStats();
            System.out.println();
        }

    }

    public void countingHouse(Scanner scnr) {
        // Gather all the coppers
        ArrayList<Card> coppers = new ArrayList<>();
        for (Card card : discardPile) {
            if (card.getName().equals("Copper")) {
                discardPile.remove(card);
                coppers.add(card);
            }
        }

        System.out.println("There are " + coppers.size() + " cards in your discard pile.");
        System.out.println("How many would you like to put into your hand?");
        String optionStr = scnr.nextLine();
        int choice = getValidDigit(scnr, optionStr, coppers.size());

        // Put the coppers in hand
        for (int i=0; i<choice; ++i) {
            hand.add(coppers.get(0));
            coppers.remove(0);
        }

        // If there are still coppers that the player didn't put in hand, discard them
        while (coppers.size() > 0) {
            discardPile.add(coppers.get(0));
            coppers.remove(0);
        }
    }

    public void feast(Scanner scnr) {
        trashCard(hand, findCard("Feast", cardsInMiddle), true);
        System.out.println();
        System.out.println("You may now gain a card costing up to $5.\n");

        Card cardGained = printOptions(scnr, cardsInMiddle, "Feast", 0);

        if (!cardGained.getName().equals("")) {
            gainCard(scnr, cardGained);
        }
    }

    public void festival() {
        System.out.println(stats.changeActions(2));
        System.out.println(stats.changeBuys(1));
        System.out.println(stats.changeMoney(2));
    }

    public void goons(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println(stats.changeMoney(2));
            System.out.println(stats.changeBuys(1));
            printAttack(scnr);
        } else {
            System.out.println("You must discard down to 3 cards.");
            while (hand.size() > 3) {
                System.out.println();
                for (int i=0; i<hand.size(); ++i) {
                    System.out.println((i+1) + ". " + hand.get(i).cardInHand());
                }
                System.out.println("\nChoose a card to discard (" + (hand.size() - 3) + " left):");
                String optionStr = scnr.nextLine();
                int option = getValidDigit(scnr, optionStr, hand.size());
                Card cardToDiscard = hand.get(option - 1);
                discardCard(hand, cardToDiscard, true);
            }
            System.out.println("\nNew hand stats:");
            printStats();
        }
    }

    public void harbinger(Scanner scnr) {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(1));

        System.out.println("You may now pick a card from your discard pile to add to your deck.");
        if (discardPile.size() > 0) {
            Card cardToAdd = printOptions(scnr, discardPile, "Harbinger", 0);
            System.out.println("Placing the " + cardToAdd.getName() + " card in your draw pile...");
            drawPile.add(0, cardToAdd);
            discardPile.remove(cardToAdd);
            System.out.println("Done.\n");
        } else {
            System.out.println("Sorry, you have no cards in your discard pile.\n");
        }

    }

    public void junkDealer(Scanner scnr) {
        System.out.println(stats.changeActions(1));
        System.out.println(stats.changeMoney(1));
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println("Choose a card to trash.\n");
        Card cardToTrash = printOptions(scnr, hand, "Junk Dealer", 0);
        if (!cardToTrash.getType().equals("")) {
            trashCard(hand, cardToTrash, true);
        }
    }

    public void laboratory() {
        System.out.println(stats.changeActions(1));
        printStats();
        System.out.println();
        System.out.println("Drawing 2 cards...");
        for (int i=0; i<2; ++i) {
            drawCard(true);
        }
    }

    public void library(Scanner scnr) {
        while (hand.size() < 7) {
            Card card = drawCard(true);
            if (card.getType().contains("Action")) {
                System.out.println("Would you like to discard this card? (1) yes, (2) no:");
                String optionStr = scnr.nextLine();
                int choice = getValidDigit(scnr, optionStr, 2);
                if (choice == 1) {
                    discardCard(hand, card, true);
                }
            }
        }
    }

    public void menagerie() {
        System.out.println(stats.changeActions(1));

        boolean differentCards = true;
        for (int i=0; i<hand.size(); ++i) {
            for (int j=1; j<hand.size(); ++j) {
                if (hand.get(j).getName().equals(hand.get(i).getName())) {
                    differentCards = false;
                    break;
                }
            }
        }

        if (differentCards) {
            System.out.println("Your hand contains all different cards. Drawing 3 cards...");
            for (int i=0; i<3; ++i) {
                System.out.println("Drawing a card...");
                drawCard(true);
            }
        } else {
            System.out.println("Your hand does not contain all different cards.\n");
            System.out.println("Drawing a card...");
            drawCard(true);
        }

    }

    public void market() {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(1));
        System.out.println(stats.changeBuys(1));
        System.out.println(stats.changeMoney(1));
    }

    public void merchant() {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(1));
    }

    public void militia(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println(stats.changeMoney(2));
            printAttack(scnr);
        } else {
            System.out.println("You must discard down to 3 cards.");
            while (hand.size() > 3) {
                System.out.println();
                for (int i=0; i<hand.size(); ++i) {
                    System.out.println((i+1) + ". " + hand.get(i).cardInHand());
                }
                System.out.println("\nChoose a card to discard (" + (hand.size() - 3) + " left):");
                String optionStr = scnr.nextLine();
                int option = getValidDigit(scnr, optionStr, hand.size());
                Card cardToDiscard = hand.get(option - 1);
                discardCard(hand, cardToDiscard, true);
            }
            System.out.println("\nNew hand stats:");
            printStats();
        }
    }

    public void mill(Scanner scnr) {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(1));

        System.out.println("Would you like to discard 2 cards? (1) yes, (2) no");
        String optionStr = scnr.nextLine();
        int choice = getValidDigit(scnr, optionStr, 2);
        if (choice == 1) {
            System.out.println("Choose 2 cards to discard.\n");
            for (int i=0; i<2; ++i) {
                System.out.println("Enter card " + (i+1) + ".");
                Card cardToDiscard = printOptions(scnr, hand, "Mill", 0);
                discardCard(hand, cardToDiscard, true);
            }

            System.out.println(stats.changeMoney(2));
        }
    }

    public void mine(Scanner scnr) {
        System.out.println("Choose a treasure card from your hand to trash.\n");

        Card cardTrashed = printOptions(scnr, hand, "Mine", 0);
        if (!cardTrashed.getType().equals("")) {
            trashCard(hand, cardTrashed, true);
        }

        // Gain a card costing 3 more
        Card cardToGain = new Card();
        if (cardTrashed.getName().equals("Copper")) {
            cardToGain = findCard("Silver", cardsInMiddle);
        } else if (cardTrashed.getName().equals("Silver")) {
            cardToGain = findCard("Gold", cardsInMiddle);
        }

        System.out.println();
        System.out.println("Adding a " + cardToGain.getName() + " to your hand...");
        hand.add(cardToGain);
        cardToGain.decreaseNumRemaining();
        stats.changeMoney(cardToGain.getValue());
        System.out.println("Done.\n");
    }

    public void miningVillage(Scanner scnr) {
        System.out.println(stats.changeActions(2));

        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println("Would you like to trash this Mining Village card for +$2? (1) yes, (2) no:");
        String optionStr = scnr.nextLine();
        int choice = getValidDigit(scnr, optionStr, 2);
        if (choice == 1) {
            stats.changeMoney(2);
            for (Card card : actionsPlayed) {
                if (card.getName().equals("Mining Village")) {
                    card.setTrashCard(true);
                    break;
                }
            }
        }
    }

    public void moat() {
        System.out.println("Drawing 2 cards...");
        for (int i=0; i<2; ++i) {
            drawCard(true);
        }
    }

    public void moneylender() {
        boolean trashedCopper = false;
        for (Card card : hand) {
            if (card.getName().equals("Copper")) {
                trashCard(hand, card, true);
                trashedCopper = true;
                break;
            }
        }

        if (trashedCopper) {
            System.out.println(stats.changeMoney(3));
        } else {
            System.out.println("Sorry, you did not have a copper to trash. No extra money was added.");
        }

    }

    public void monument() {
        System.out.println(stats.changeMoney(2));
        System.out.println("Gaining a victory point token...");
        increasePointTokens(1);
    }

    public void mountebank(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println(stats.changeMoney(2));
            printAttack(scnr);
            System.out.println("Did your opponent gain a curse? (1) yes, (2) no:");
            String option = scnr.nextLine();
            int choice = getValidDigit(scnr, option, 2);
            if (choice == 1) {
                findCard("Curse", cardsInMiddle).decreaseNumRemaining();
            }
        } else {
            boolean hasCurse = false;
            for (Card card : hand) {
                if (card.getType().equals("Curse")) {
                    hasCurse = true;
                    discardCard(hand, card, true);
                    System.out.println("Done.");
                    break;
                }
            }
            if (!hasCurse) {
                System.out.println("You do not have a curse to discard. Gaining a curse and a copper...");
                discardPile.add(findCard("Curse", cardsInMiddle));
                findCard("Curse", cardsInMiddle).decreaseNumRemaining();
                discardPile.add(findCard("Copper", cardsInMiddle));
                findCard("Copper", cardsInMiddle).decreaseNumRemaining();
                System.out.println("Done.");
            }
        }
    }

    public void pawn(Scanner scnr) {
        System.out.println("Choose 2 of the following.");
        System.out.println("1. +1 Card\n2. +1 Action\n3. +1 Buy\n4. +$1\n");
        System.out.println("Put the numbers of the options you would like to choose, separated by a space:");
        String optionStr = scnr.nextLine();
        int choice1 = getValidDigit(scnr, optionStr, 4);
        optionStr = optionStr.substring(1);
        int choice2 = getValidDigit(scnr, optionStr, 4);
        while (choice2 == choice1) {
            System.out.println("You have already chosen this number. Try again:");
            optionStr = scnr.nextLine();
            choice2 = getValidDigit(scnr, optionStr, 4);
        }

        if ((choice1 == 1) || (choice2 == 1)) {
            System.out.println("Drawing a card...");
            drawCard(true);
        }
        if ((choice1 == 2) || (choice2 == 2)) {
            System.out.println(stats.changeActions(1));
        }
        if ((choice1 == 3) || (choice2 == 3)) {
            System.out.println(stats.changeBuys(1));
        }
        if ((choice1 == 4) || (choice2 == 4)) {
            System.out.println(stats.changeMoney(1));
        }
    }

    public void peddler() {
        System.out.println(stats.changeActions(1));
        System.out.println(stats.changeMoney(1));

        System.out.println("Drawing a card...");
        drawCard(true);
    }

    public void poacher(Scanner scnr) {

        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeMoney(1));
        System.out.println(stats.changeActions(1));

        int numEmptySupplyPiles = 0;
        for (Card card : cardsInMiddle) {
            if (card.getNumRemaining() == 0) {
                ++numEmptySupplyPiles;
            }
        }

        System.out.println();
        for (int i=0; i<numEmptySupplyPiles; ++i) {
            System.out.println("Choose a card to discard.");
            Card cardToDiscard = printOptions(scnr, hand, "Poacher", 0);
            discardCard(hand, cardToDiscard, true);
            System.out.println("Done.\n");
        }

    }

    public void remake(Scanner scnr) {
        for (int i=0; i<2; ++i) {
            System.out.println("Choose a card from your hand to trash.\n");
            Card cardTrashed = printOptions(scnr, hand, "Remake-Trash", 0);
            if (!cardTrashed.getType().equals("")) {
                trashCard(hand, cardTrashed, true);
            }

            System.out.println("Now choose a card to gain.\n");
            Card cardToGain = printOptions(scnr, cardsInMiddle, "Remake-Gain", cardTrashed.getCost());
            if (!cardToGain.getType().equals("")) {
                gainCard(scnr, cardToGain);
            }
        }
    }

    public void remodel(Scanner scnr) {
        System.out.println("Choose a card from your hand to trash.\n");
        Card cardTrashed = printOptions(scnr, hand, "Remodel-Trash", 0);
        if (!cardTrashed.getType().equals("")) {
            trashCard(hand, cardTrashed, true);
        }

        // Gain a card costing up to $2 more
        System.out.println("Now choose a card to gain.\n");
        Card cardToGain = printOptions(scnr, cardsInMiddle, "Remodel-Gain", cardTrashed.getCost());
        if (!cardToGain.getType().equals("")) {
            gainCard(scnr, cardToGain);
        }
    }

    public void sentry(Scanner scnr) {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(1));

        if (drawPile.size() < 2) {
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile.clear();
        }
        Card topCard = drawPile.get(0);
        Card secondCard = drawPile.get(1);
        System.out.println("The top 2 cards of your deck are " + topCard.getName() + " and " + secondCard.getName() + ".\n");
        drawPile.remove(topCard);
        drawPile.remove(secondCard);

        System.out.println("Which card would you like to trash, discard, or put back first?");
        System.out.println("1. " + topCard.getName() + "\n2. " + secondCard.getName());
        String whichCardStr = scnr.nextLine();
        int whichCard = getValidDigit(scnr, whichCardStr, 2);

        System.out.println();
        for (int i=0; i<2; ++i) {
            if (i == 0) {
                System.out.println("What would you like to do with it?");
            } else {
                System.out.println("What would you like to do with the other card?");
            }
            System.out.println("1. Trash\n2. Discard\n3. Put it back");
            String optionStr = scnr.nextLine();
            int choice = getValidDigit(scnr, optionStr, 3);
            if (choice == 1) {
                switch (whichCard) {
                    case 1:
                        System.out.println("Trashing the " + topCard.getName() + " card...");
                        System.out.println("Done.");
                        break;
                    case 2:
                        System.out.println("Trashing the " + secondCard.getName() + " card...");
                        System.out.println("Done.");
                        break;
                }
            } else if (choice == 2) {
                switch (whichCard) {
                    case 1:
                        System.out.println("Discarding the " + topCard.getName() + " card...");
                        discardPile.add(topCard);
                        System.out.println("Done.");
                        break;
                    case 2:
                        System.out.println("Discarding the " + secondCard.getName() + " card...");
                        discardPile.add(secondCard);
                        System.out.println("Done.");
                        break;
                }
            } else {
                switch (whichCard) {
                    case 1:
                        System.out.println("Putting it back in the draw pile...");
                        drawPile.add(0, topCard);
                        break;
                    case 2:
                        System.out.println("Putting it back in the draw pile...");
                        drawPile.add(0, secondCard);
                        break;
                }

            }
            if (whichCard == 1) {
                whichCard = 2;
            } else {
                whichCard = 1;
            }
        }

    }

    public void shantyTown() {
        System.out.println(stats.changeActions(2));

        boolean hasAction = false;
        for (Card card : hand) {
            if (card.getType().contains("Action")) {
                hasAction = true;
                break;
            }
        }

        if (!hasAction) {
            System.out.println("You have no action cards. Drawing 2 cards...");
            for (int i=0; i<2; ++i) {
                drawCard(true);
            }
        }

    }

    public void smithy() {
        System.out.println("Drawing 3 cards...");
        for (int i=0; i<3; ++i) {
            drawCard(true);
        }
    }

    public void spy(Scanner scnr, boolean opponent) {
        String prompt = "Would your opponent like you to (1) keep it there or (2) discard it?";

        if (drawPile.size() < 1) {
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile.clear();
        }
        if (!opponent) {
            prompt = "Would you like to (1) keep it there or (2) discard it?";
            System.out.println("Drawing a card...");
            drawCard(true);

            System.out.println(stats.changeActions(1));

            printAttack(scnr);
        }

        System.out.println("The top card of your deck is: " + drawPile.get(0).getName() + ".");
        System.out.println(prompt);
        String optionStr = scnr.nextLine();
        int choice = getValidDigit(scnr, optionStr, 2);
        if (choice == 2) {
            discardCard(drawPile, drawPile.get(0), false);
        }
    }

    public void steward(Scanner scnr) {
        System.out.println("1. +2 Cards\n2. +$2\n3. Trash 2 cards from your hand.\n");
        System.out.println("Choose an option:");
        String choiceStr = scnr.nextLine();
        int choice = getValidDigit(scnr, choiceStr, 3);

        if (choice == 1) {
            System.out.println("Drawing 2 cards...");
            for (int i=0; i<2; ++i) {
                drawCard(true);
            }
        } else if (choice == 2) {
            System.out.println(stats.changeMoney(2));
        } else {
            System.out.println("Trash 2 cards from your hand.\n");
            trashMultiple(scnr, 2);
        }

    }

    public void thief(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("Your opponent must now choose option 2 on their menu to be affected by your attack.");
            System.out.println("Did your opponent reveal any treasure cards? (1) yes, (2) no:");
            String choiceStr = scnr.nextLine();
            int choice = getValidDigit(scnr, choiceStr, 2);

            if (choice == 1) {
                System.out.println();
                System.out.println("Which treasure card did your opponent reveal?");
                System.out.println("1. Copper");
                System.out.println("2. Silver");
                System.out.println("3. Gold");

                System.out.println("\nChoose an option:");
                String trashedStr = scnr.nextLine();
                int trashed = getValidDigit(scnr, trashedStr, 3);

                System.out.println("\nWould you like to gain this card? (1) yes, (2) no:");
                String gainStr = scnr.nextLine();
                int gain = getValidDigit(scnr, gainStr, 2);

                if (gain == 1) {
                    switch (trashed) {
                        case 1:
                            gainCard(scnr, findCard("Copper"));
                            break;
                        case 2:
                            gainCard(scnr, findCard("Silver"));
                            break;
                        case 3:
                            gainCard(scnr, findCard("Gold"));
                            break;
                    }
                }
            }
        } else {
            if (drawPile.size() < 2) {  // Make sure there are enough cards to draw
                Collections.shuffle(discardPile);
                drawPile.addAll(discardPile);
                discardPile.clear();
            }
            Card topCard = drawPile.get(0);
            Card secondCard = drawPile.get(1);

            int whichIsTreasure = -1;
            if (topCard.getType().contains("Treasure")) {
                whichIsTreasure = 1;
            } else if (secondCard.getType().contains("Treasure")) {
                whichIsTreasure = 2;
            }
            if (topCard.getType().contains("Treasure") && secondCard.getType().contains("Treasure")) {
                whichIsTreasure = 3;
            }

            System.out.println("The top 2 cards of your deck are the " + topCard.getName() + " card and the " +
                    secondCard.getName() + " card.");

            drawPile.remove(topCard);
            drawPile.remove(secondCard);

            System.out.println();
            if ((whichIsTreasure == 1) || (whichIsTreasure == 2)) {
                System.out.println("You have a treasure card. Would your opponent like you to trash it? (1) yes, (2) no:");
                String optionStr = scnr.nextLine();
                int option = getValidDigit(scnr, optionStr, 2);
                if (option == 1) {
                    if (whichIsTreasure == 1) {
                        System.out.println("Trashing the " + topCard.getName() + " card...");
                        System.out.println("Discarding the " + secondCard.getName() + " card...");
                        discardPile.add(secondCard);
                    } else {
                        System.out.println("Trashing the " + secondCard.getName() + " card...");
                        System.out.println("Discarding the " + topCard.getName() + " card...");
                        discardPile.add(topCard);
                    }
                    System.out.println("Done.");
                }
            } else if (whichIsTreasure == 3) {
                System.out.println("You have 2 treasure cards.");
                System.out.println("What would your opponent like you to do?");
                System.out.println("1. Trash the " + topCard.getName() + " card.");
                System.out.println("2. Trash the " + secondCard.getName() + " card.");
                System.out.println("3. Keep both cards.");
                String optionStr = scnr.nextLine();
                int option = getValidDigit(scnr, optionStr, 3);
                if (option == 1) {
                    System.out.println("Trashing the " + topCard.getName() + " card...");
                    System.out.println("Discarding the " + secondCard.getName() + " card...");
                    discardPile.add(secondCard);
                    System.out.println("Done.");
                } else if (option == 2) {
                    System.out.println("Trashing the " + secondCard.getName() + " card...");
                    System.out.println("Discarding the " + topCard.getName() + " card...");
                    discardPile.add(topCard);
                    System.out.println("Done.");
                }
            } else {
                System.out.println("You have no treasure cards. Discarding them...");
                discardPile.add(topCard);
                discardPile.add(secondCard);
                System.out.println("Done.");
            }
            System.out.println();

        }

    }

    public Card throneRoomKingCourt(Scanner scnr, int numPlays) {
        boolean hasAction = false;
        for (Card card : hand) {
            if (card.getType().contains("Action")) {
                hasAction = true;
                break;
            }
        }

        if (hasAction) {
            System.out.println("Pick an action to play " + numPlays + " times.\n");

            Card action = printOptions(scnr, hand, "Throne Room/King's Court", 0);
            hand.remove(action);

            for (int i=0; i<numPlays; ++i) {
                action.increaseNumDurationPlays();
                System.out.println("\nPlaying the " + action.getName() + " card...\n");
                dominion.playAction(scnr, action, this);
            }
            action.decreaseNumDurationPlays();
            return action;
        } else {
            System.out.println("You have no action cards to play.\n");
            return new Card();
        }
    }

    public void torturer(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("Drawing 3 cards...");
            for (int i=0; i<3; ++i) {
                drawCard(true);
            }
            System.out.println("Your opponent may now choose option 2 of their menu to be affected by your attack.");
            System.out.println("Press enter when they have done so:");
            scnr.nextLine();
        } else {
            System.out.println("Do you choose to (1) discard 2 cards, or (2) gain a curse to your hand?");
            System.out.println("Enter your choice:");
            String optionStr = scnr.nextLine();
            int choice = getValidDigit(scnr, optionStr, 2);

            if (choice == 1) {
                System.out.println("Choose 2 cards to discard.\n");
                for (int i=0; i<2; ++i) {
                    System.out.println("Enter card " + (i+1) + ".");
                    Card cardToDiscard = printOptions(scnr, hand, "Vault", 0);
                    discardCard(hand, cardToDiscard, true);
                }
            } else {
                gainCard(scnr, findCard("Curse"));
            }
        }
    }

    public void upgrade(Scanner scnr) {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(1));

        System.out.println("Choose a card from your hand to trash.\n");
        Card cardTrashed = printOptions(scnr, hand, "Remodel-Trash", 0);
        if (!cardTrashed.getType().equals("")) {
            trashCard(hand, cardTrashed, true);
        }

        // Gain a card costing up to $1 more
        System.out.println("Now choose a card to gain.\n");
        Card cardToGain = printOptions(scnr, cardsInMiddle, "Remake-Gain", cardTrashed.getCost());
        if (!cardToGain.getType().equals("")) {
            gainCard(scnr, cardToGain);
        }
    }

    public void vassal(Scanner scnr) {
        System.out.println(stats.changeMoney(2));

        if (drawPile.size() < 1) {
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile.clear();
        }
        Card topCard = drawPile.get(0);
        System.out.println("The top card of your deck is " + topCard.getName() + ".\n");
        drawPile.remove(topCard);

        if (topCard.getType().contains("Action")) {
            System.out.println("It is an action card. Playing it now...\n");
            dominion.playAction(scnr, topCard, this);
        } else {
            System.out.println("It is not an action. Discarding it...");
            System.out.println("Done.\n");
        }

        discardPile.add(topCard);
    }

    public void vault(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("Drawing 2 cards...");
            for (int i=0; i<2; ++i) {
                drawCard(true);
            }

            ArrayList<Card> options = new ArrayList<>();

            int option = 1;
            for (Card card : hand) {
                System.out.println(option + ". " + card);
                options.add(card);
                ++option;
            }

            System.out.println();

            System.out.println("Choose the number(s) of the card(s) you would like to discard, each separated by a space:");
            String choices = scnr.nextLine();
            Scanner scanChoices = new Scanner(choices);

            int numDiscarded = 0;
            while (scanChoices.hasNextInt()) {
                option = scanChoices.nextInt();
                System.out.println();
                Card chosenCard = options.get(option - 1);
                discardCard(hand, chosenCard, true);
                ++numDiscarded;
            }

            if (numDiscarded > 0) {
                System.out.println(stats.changeMoney(numDiscarded));
            }

            System.out.println("Your opponent may now choose option 2 of their menu to discard 2 cards and draw 1.");
            System.out.println("Press enter when they have done so:");
        } else {
            System.out.println("Would you like to discard 2 cards to draw a new card? (1) yes, (2) no:");
            String optionStr = scnr.nextLine();
            int choice = getValidDigit(scnr, optionStr, 2);
            if (choice == 1) {
                System.out.println("Choose 2 cards to discard.\n");
                for (int i=0; i<2; ++i) {
                    System.out.println("Enter card " + (i+1) + ".");
                    Card cardToDiscard = printOptions(scnr, hand, "Vault", 0);
                    discardCard(hand, cardToDiscard, true);
                }

                System.out.println("Drawing a replacement card...");
                drawCard(true);
            }
        }
    }

    public void village() {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(2));
    }

    public void witch(Scanner scnr, boolean opponent) {
        if (!opponent) {
            System.out.println("Drawing 2 cards...");
            for (int i=0; i<2; ++i) {
                drawCard(true);
            }

            printAttack(scnr);
            findCard("Curse", cardsInMiddle).decreaseNumRemaining();
        } else {
            gainCard(scnr, findCard("Curse"));
        }
    }

    public void wanderingMinstrel(Scanner scnr) {
        System.out.println("Drawing a card...");
        drawCard(true);

        System.out.println(stats.changeActions(2));

        if (drawPile.size() < 3) {  // Make sure there are enough cards to draw
            Collections.shuffle(discardPile);
            drawPile.addAll(discardPile);
            discardPile.clear();
        }
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(drawPile.get(0));
        cards.add(drawPile.get(1));
        cards.add(drawPile.get(2));

        System.out.println("The top 3 cards of your deck are: " + cards.get(0).getName() + ", " + cards.get(1).getName()
                + ", and " + cards.get(2).getName() + ".");

        int numActions = 0;
        for (int i=0; i<3; ++i) {
            drawPile.remove(cards.get(numActions));
            if (!cards.get(numActions).getType().contains("Action")) {
                System.out.println("The " + cards.get(numActions).getName() + " card is not an action. Discarding it...");
                discardPile.add(cards.get(numActions));
                cards.remove(cards.get(numActions));
                System.out.println("Done.\n");
            } else {
                ++numActions;
            }
        }

        if (cards.size() > 0) {
            System.out.println("You have at least one action card.");
            while (cards.size() > 1) {
                System.out.println("Which would you like to put back now?");
                Card putBack = printOptions(scnr, cards, "Wandering Minstrel", 0);
                System.out.println("Putting the " + putBack.getName() + " card back in the draw pile...");
                drawPile.add(0, putBack);
                cards.remove(putBack);
                System.out.println("Done.\n");
            }
            System.out.println("Putting the " + cards.get(0).getName() + " card back in the draw pile...");
            drawPile.add(0, cards.get(0));
            System.out.println("Done.\n");
        }

        cards.clear();
    }

    public void watchtower() {
        while (hand.size() < 6) {
            System.out.println("Drawing a card...");
            drawCard(true);
        }
    }

    public void wharf() {
        for (int i=0; i<2; ++i) {
            System.out.println("Drawing a card...");
            drawCard(true);
        }
        System.out.println(stats.changeBuys(1));
    }

    public void woodcutter() {
        System.out.println(stats.changeBuys(1));
        System.out.println(stats.changeMoney(2));
    }

    public void workshop(Scanner scnr) {
        System.out.println("You may gain a card costing up to $4.\n");

        Card cardGained = printOptions(scnr, cardsInMiddle, "Workshop", 0);
        if (!cardGained.getType().equals("")) {
            gainCard(scnr, cardGained);
        }
    }


    /**
     *
     * This method is called each time an action is played
     * Checks if the action played was a merchant, and if so, it adds to the number of unused merchants for that turn
     * @return the new number of unused merchants
     */
    public int checkMerchant(Card actionPlayed, int unusedMerchants) {
        if (actionPlayed.getName().equals("Merchant")) {
            ++unusedMerchants;
        }
        return useMerchant(hand, unusedMerchants);
    }

    /**
     *
     * This method is called each time an action is played
     * Uses the merchant if there is currently one unused, and a silver in play
     * @return the new number of unused merchants
     */
    public int useMerchant(ArrayList<Card> hand, int unusedMerchants) {
        boolean hasSilver = hand.contains(findCard("Silver", hand));

        // If merchant(s) were played this round and there is a silver in hand, add +$1 for each unused merchant
        if (unusedMerchants > 0 && hasSilver) {
            for (int i=0; i<unusedMerchants; ++i) {
                System.out.println("You now have a silver and merchant in play.");
                System.out.println(stats.changeMoney(1));
                System.out.println("Done.\n");
            }
            unusedMerchants = 0;
            System.out.println("New stats:");
            printStats();
        }

        return unusedMerchants;
    }

    /**
     *
     * Checks for and plays any leftover duration cards at the start of a new turn
     */
    public void playDuration(Scanner scnr) {
        for (Card card : actionsPlayed) {
            if (card.getType().contains("Duration")) {
                for (int i=0; i<card.getNumDurationPlays(); ++i) {
                    System.out.println("Playing the " + card.getName() + " duration card...");
                    dominion.playAction(scnr, card, this);
                }
                System.out.println("\nNew stats:");
                printStats();
            }
            card.setNumDurationPlays(0);
        }
    }
}
