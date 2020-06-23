import java.io.FileNotFoundException;
import java.util.*;
import java.io.File;

public class Dominion {
    // **************************** MAIN GAME CYCLE METHODS ****************************
    public static void main(String[] args) throws FileNotFoundException {
        GameManager gameManager = new GameManager();

        File file = new File("iofiles/dominionCards.txt");
        Scanner fileScan = new Scanner(file);
        loadCards(fileScan, gameManager);

        Scanner scnr = new Scanner(System.in);
        Random rand = new Random();
        rand.setSeed(2797);

        boolean usingProsperity = setup(scnr, gameManager);

        System.out.println("Press enter to begin your first turn.");
        scnr.nextLine();

        takeTurns(scnr, gameManager, usingProsperity);

        endGame(gameManager, usingProsperity);
    }

    public static void loadCards(Scanner fileScan, GameManager gameManager) {
        fileScan.useDelimiter("\n\n");

        // Reading from the input file to add all the cards
        while (fileScan.hasNext()) {
            String line = fileScan.next();
            String[] cardStats = line.split(",");
            Card card = new Card(cardStats[0].trim(),
                    cardStats[1].trim(),
                    cardStats[2].trim(),
                    Integer.parseInt(cardStats[3].trim()),
                    Integer.parseInt(cardStats[4].trim()),
                    Integer.parseInt(cardStats[5].trim()),
                    0);
            gameManager.addToAllCards(card);
        }

        // Printing out all the kingdom cards and their descriptions so the user can choose
        gameManager.printKingdomCards();
    }

    public static boolean setup(Scanner scnr, GameManager gameManager) {
        System.out.println("Welcome! Please choose a game setup\n");

        List<ArrayList<String>> dominionSets = new ArrayList<>();

        dominionSets.add(new ArrayList<>(Arrays.asList("Cellar", "Market", "Militia", "Mine", "Moat",
                "Remodel", "Smithy", "Village", "Woodcutter", "Workshop")));
        dominionSets.add(new ArrayList<>(Arrays.asList("Adventurer", "Bureaucrat", "Chancellor", "Chapel", "Feast",
                "Laboratory", "Market", "Mine", "Moneylender", "Throne Room")));
        dominionSets.add(new ArrayList<>(Arrays.asList("Bureaucrat", "Chancellor", "Council Room", "Festival", "Library",
                "Militia", "Moat", "Spy", "Thief", "Village")));
        dominionSets.add(new ArrayList<>(Arrays.asList("Cellar", "Chapel", "Feast", "Gardens", "Laboratory", "Thief",
                "Village", "Witch", "Woodcutter", "Workshop")));
        dominionSets.add(new ArrayList<>(Arrays.asList("Bureaucrat", "Cellar", "Festival", "Library", "Market", "Remodel",
                "Smithy", "Throne Room", "Village", "Woodcutter")));
        dominionSets.add(new ArrayList<>(Arrays.asList("Bureaucrat", "Cellar", "Festival", "Library", "Market", "Remodel",
                "Smithy", "Throne Room", "Village", "Woodcutter")));
        dominionSets.add(new ArrayList<>(Arrays.asList("Baron", "Harem", "Pawn", "Shanty Town", "Upgrade", "Bishop",
                "Counting House", "Goons", "Monument", "Peddler")));
        dominionSets.add(new ArrayList<>(Arrays.asList("Hoard", "Talisman", "Bishop", "Vault", "Watchtower", "Bridge", "Mill",
                "Mining Village", "Pawn", "Torturer")));

        System.out.println("Original Dominion Sets");
        gameManager.printSet("1. First Game: ", dominionSets.get(0));
        gameManager.printSet("2. Big Money: ", dominionSets.get(1));
        gameManager.printSet("3. Interaction: ", dominionSets.get(2));
        gameManager.printSet("4. Size Distortion: ", dominionSets.get(3));
        gameManager.printSet("5. Village Square: ", dominionSets.get(4));
        System.out.println();

        System.out.println("Intrigue + Prosperity Expansions");
        gameManager.printSet("6. Paths to Victory: ", dominionSets.get(5));
        gameManager.printSet("7. All Along the Watchtower: ", dominionSets.get(6));
        System.out.println();

        System.out.println("Other");
        System.out.println("8. Custom");
        System.out.println("9. Random");

        // Filling the cards in "middle" with the action cards chosen by the user
        System.out.println("\nType the corresponding number:");
        String setupStr = scnr.nextLine();
        int setup = gameManager.getValidDigit(scnr, setupStr, 9);

        if (setup == 8) {            // Custom
            System.out.println();
            for (int i = 0; i < 10; ++i) {
                System.out.println("Type the name kingdom card #" + (i + 1));
                String name = scnr.nextLine();
                while (gameManager.notValid(name)) {
                    System.out.println("Not a valid card, or you may have already entered this card. Try again:");
                    name = scnr.nextLine();
                }
                gameManager.addToCardsInMiddle(name);
            }
        } else if (setup == 9){     // Random
            gameManager.generateRandom();
        } else {                    // Preset
            gameManager.addToCardsInMiddle(setup, dominionSets);
        }

        // Printing out the user's choice of kingdom cards
        gameManager.printCardsInMiddle();

        // Fill cardsInMiddle with the core cards
        boolean usingProsperity = false;
        System.out.println("Are you playing with the prosperity expansion? (1) yes, (2) no");
        String optionStr = scnr.nextLine();
        int choice = gameManager.getValidDigit(scnr, optionStr, 2);

        gameManager.addToCardsInMiddle("Copper");
        gameManager.addToCardsInMiddle("Silver");
        gameManager.addToCardsInMiddle("Gold");
        if (choice == 1) {
            usingProsperity = true;
            gameManager.addToCardsInMiddle("Platinum");
            gameManager.addToCardsInMiddle("Colony");
        }
        gameManager.addToCardsInMiddle("Province");
        gameManager.addToCardsInMiddle("Duchy");
        gameManager.addToCardsInMiddle("Estate");
        gameManager.addToCardsInMiddle("Curse");

        // Adding to the draw pile and shuffling
        System.out.println("\nPerfect!");
        System.out.println("Drawing 7 copper and 3 estate cards...");
        for (int j = 0; j < 7; ++j) {
            gameManager.addToDrawPile("Copper");
        }
        for (int j = 0; j < 3; ++j) {
            gameManager.addToDrawPile("Estate");
        }

        System.out.println("Shuffling...");
        gameManager.shuffleDrawPile();
        System.out.println("Ready.\n");

        return usingProsperity;
    }

    /**
     *
     * Take turns until the number of provinces run out
     */
    public static void takeTurns(Scanner scnr, GameManager gameManager, boolean usingProsperity) {
        boolean condition;

        if (usingProsperity) {
            condition = (gameManager.findCard("Colony").getNumRemaining() > 0);
        } else {
            condition = (gameManager.findCard("Province").getNumRemaining() > 0);
        }

        // Keep taking turns as long as there are provinces or colonies left (depending on prosperity condition)
        while (condition) {
            // Draw a hand of 5 cards, calculate stats, display to player
            gameManager.drawNewHand();

            // Print money, actions, and buys
            System.out.println();
            gameManager.printStats();

            // Play any leftover duration cards
            gameManager.playDuration(scnr);

            // Let the player choose menu options
            System.out.println();
            manageMenu(scnr, gameManager, usingProsperity);

            // Discard the used hand and cards played, and reset the stats
            gameManager.reset();

            // Test if the game is over (besides provinces, game may also end if 3 action card stacks are empty)
            if (gameManager.isGameOver()) {
                break;
            }

            // Update the condition
            if (usingProsperity) {
                condition = (gameManager.findCard("Colony").getNumRemaining() > 0);
            } else {
                condition = (gameManager.findCard("Province").getNumRemaining() > 0);
            }
        }

    }

    /**
     *
     * Cycle through the menu choices until the player ends their turn
     */
    public static void manageMenu(Scanner scnr, GameManager gameManager, boolean usingProsperity) {
        gameManager.printMenuOptions(usingProsperity);
        String menuChoiceStr = scnr.nextLine();
        int menuChoice = gameManager.getValidDigit(scnr, menuChoiceStr, 5);
        int unusedMerchants = 0;

        while (menuChoice != 5) {

            // Keeps track of whether the menu options should be reprinted after user chooses a number
            boolean noPrintOptions = false;

            if (menuChoice == 1) {  // Opponent bought province or colony
                gameManager.boughtEndingVictory(usingProsperity, true);
            } else if (menuChoice == 2) {   // Opponent played attack card or council room
                playedAttackCard(scnr, gameManager);
            } else if (menuChoice == 3) {   // Choose and play an action from your hand
                Card card = gameManager.chooseAction(scnr);
                unusedMerchants = gameManager.checkMerchant(card, unusedMerchants);
            } else if (menuChoice == 4) {   // Buy a card
                noPrintOptions = gameManager.buyCard(scnr, usingProsperity);
            }

            if (usingProsperity) {
                if (gameManager.findCard("Colony").getNumRemaining() == 0) {
                    break;
                }
            } else {
                if (gameManager.findCard("Province").getNumRemaining() == 0) {
                    break;
                }
            }

            if (!noPrintOptions) {
                gameManager.printMenuOptions(usingProsperity);
            }
            menuChoiceStr = scnr.nextLine();
            menuChoice = gameManager.getValidDigit(scnr, menuChoiceStr, 5);
        }

        System.out.println();
    }

    public static void endGame(GameManager gameManager, boolean usingProsperity) {
        System.out.println("\n\n------------------------------- GAME OVER -------------------------------");

        int endPoints = gameManager.calculateEndPoints(usingProsperity);

        System.out.println("Total points: " + endPoints);
    }


    /**
     *
     * Manages the function calls for the correct action card
     * @return the card played (for purposes of the merchant/duration cards)
     */
    public Card playAction(Scanner scnr, Card cardPlayed, GameManager gameManager) {

        switch (cardPlayed.getName()) {
            case "Adventurer":
                gameManager.adventurer();
                break;
            case "Ambassador":
                gameManager.ambassador(scnr, false);
                break;
            case "Bandit":
                gameManager.bandit(scnr, false);
                break;
            case "Baron":
                gameManager.baron(scnr);
                break;
            case "Bishop":
                gameManager.bishop(scnr, false);
                break;
            case "Bridge":
                gameManager.bridge();
                break;
            case "Border Village":
                gameManager.borderVillage();
                break;
            case "Bureaucrat":
                gameManager.bureaucrat(scnr, false);
                break;
            case "Cellar":
                gameManager.cellar(scnr);
                break;
            case "Chancellor":
                gameManager.chancellor();
                break;
            case "Chapel":
                gameManager.chapel(scnr);
                break;
            case "Council Room":
                gameManager.councilRoom(scnr, false);
                break;
            case "Counting House":
                gameManager.countingHouse(scnr);
                break;
            case "Feast":
                gameManager.feast(scnr);
                break;
            case "Festival":
                gameManager.festival();
                break;
            case "Goons":
                gameManager.goons(scnr, false);
                break;
            case "Harbinger":
                gameManager.harbinger(scnr);
                break;
            case "Junk Dealer":
                gameManager.junkDealer(scnr);
                break;
            case "King's Court":
                cardPlayed = gameManager.throneRoomKingCourt(scnr, 3);
                break;
            case "Laboratory":
                gameManager.laboratory();
                break;
            case "Library":
                gameManager.library(scnr);
                break;
            case "Market":
                gameManager.market();
                break;
            case "Menagerie":
                gameManager.menagerie();
                break;
            case "Merchant":
                gameManager.merchant();
                break;
            case "Militia":
                gameManager.militia(scnr, false);
                break;
            case "Mill":
                gameManager.mill(scnr);
                break;
            case "Mine":
                gameManager.mine(scnr);
                break;
            case "Mining Village":
                gameManager.miningVillage(scnr);
                break;
            case "Moat":
                gameManager.moat();
                break;
            case "Moneylender":
                gameManager.moneylender();
                break;
            case "Monument":
                gameManager.monument();
                break;
            case "Mountebank":
                gameManager.mountebank(scnr, false);
                break;
            case "Pawn":
                gameManager.pawn(scnr);
                break;
            case "Peddler":
                gameManager.peddler();
                break;
            case "Poacher":
                gameManager.poacher(scnr);
                break;
            case "Remake":
                gameManager.remake(scnr);
                break;
            case "Remodel":
                gameManager.remodel(scnr);
                break;
            case "Sentry":
                gameManager.sentry(scnr);
                break;
            case "Shanty Town":
                gameManager.shantyTown();
                break;
            case "Smithy":
                gameManager.smithy();
                break;
            case "Spy":
                gameManager.spy(scnr, false);
                break;
            case "Steward":
                gameManager.steward(scnr);
                break;
            case "Thief":
                gameManager.thief(scnr, false);
                break;
            case "Throne Room":
                cardPlayed = gameManager.throneRoomKingCourt(scnr,2);
                break;
            case "Torturer":
                gameManager.torturer(scnr, false);
                break;
            case "Upgrade":
                gameManager.upgrade(scnr);
                break;
            case "Vassal":
                gameManager.vassal(scnr);
                break;
            case "Vault":
                gameManager.vault(scnr, false);
                break;
            case "Village":
                gameManager.village();
                break;
            case "Wandering Minstrel":
                gameManager.wanderingMinstrel(scnr);
                break;
            case "Watchtower":
                gameManager.watchtower();
                break;
            case "Wharf":
                gameManager.wharf();
                break;
            case "Witch":
                gameManager.witch(scnr, false);
                break;
            case "Woodcutter":
                gameManager.woodcutter();
                break;
            case "Workshop":
                gameManager.workshop(scnr);
                break;
        }
        return cardPlayed;
    }

    /**
     *
     * Manages the function calls for the correct action-attack card
     * "opponent" parameter means that your OPPONENT played the card on you
     */
    public static void playedAttackCard(Scanner scnr, GameManager gameManager) {

        System.out.println("\nWhich of these cards did your opponent play?");
        System.out.println("1. Bureaucrat\n2. Militia\n3. Spy\n4. Thief\n5. Witch\n6. Mountebank\n7. Ambassador\n8. " +
                "Bandit\n9. Torturer\n10. Goons\n11. Council Room\n12. Bishop\n13. Vault");
        System.out.println("\nChoose an option:");

        String optionStr = scnr.nextLine();
        int choice = gameManager.getValidDigit(scnr, optionStr, 13);
        switch (choice) {
            case 1:
                gameManager.bureaucrat(scnr, true);
                break;
            case 2:
                gameManager.militia(scnr, true);
                break;
            case 3:
                gameManager.spy(scnr, true);
                break;
            case 4:
                gameManager.thief(scnr, true);
                break;
            case 5:
                gameManager.witch(scnr, true);
                break;
            case 6:
                gameManager.mountebank(scnr, true);
                break;
            case 7:
                gameManager.ambassador(scnr, true);
                break;
            case 8:
                gameManager.bandit(scnr, true);
                break;
            case 9:
                gameManager.torturer(scnr, true);
                break;
            case 10:
                gameManager.goons(scnr, true);
                break;
            case 11:
                gameManager.councilRoom(scnr, true);
                break;
            case 12:
                gameManager.bishop(scnr, true);
                break;
            case 13:
                gameManager.vault(scnr, true);
                break;
        }
    }

}
