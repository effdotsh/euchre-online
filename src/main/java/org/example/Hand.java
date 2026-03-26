package org.example;


import java.util.Optional;

import static org.example.Euchre.NUM_PLAYERS;

public class Hand {
    private static final int NUM_CARDS_PER_HAND = 5;

    private final Player[] players;
    private final Deck deck;

    private int blueTricks = 0;
    private int redTricks = 0;

    private Suit trump;

    private final int dealerIdx;
    private int leaderIdx;
    private int callerIdx;

    public Hand(Player[] players, int dealerIdx) {
        this.players = players;
        this.dealerIdx = dealerIdx;
        this.leaderIdx = (dealerIdx + 1) % NUM_PLAYERS;
        this.deck = Deck.createDeck();
    }


    public int[] playHand() {
        deal();
        selectCallerAndTrump();

        for (int trickIdx = 0; trickIdx < NUM_CARDS_PER_HAND; trickIdx++) {
            int trickWinnerIdx = playTrick();
            if ((trickWinnerIdx % 2) == 0) {
                blueTricks++;
            } else {
                redTricks++;
            }
            leaderIdx = trickWinnerIdx;
            Player trickWinner = players[trickWinnerIdx];
            System.out.println(trickWinner.getName() + " won the trick\n\n");
        }

        return scoreHand();
    }

    private void deal() {
        for (Player player : players) {
            player.setHand(deck.draw(NUM_CARDS_PER_HAND));
        }
    }

    private int[] scoreHand() {
        //todo: add way to go alone
        boolean blueCalled = (callerIdx % 2) == 0;
        int callingTeamTricks = blueCalled ? blueTricks : redTricks;

        int bluePoints = 0;
        int redPoints = 0;

        if (callingTeamTricks == 5) {
            if (blueCalled) bluePoints = 2;
            else redPoints = 2;
        } else if (callingTeamTricks >= 3) {
            if (blueCalled) bluePoints = 1;
            else redPoints = 1;
        } else {
            if (blueCalled) redPoints = 2;
            else bluePoints = 2;
        }

        return new int[]{bluePoints, redPoints};
    }

    private int playTrick() {
        Card[] trickCards = new Card[NUM_PLAYERS];
        Optional<Suit> suitLead = Optional.empty();
        for (int offset = 0; offset < NUM_PLAYERS; offset++) {
            int playerIdx = (leaderIdx + offset) % NUM_PLAYERS;
            Player player = players[playerIdx];
            Card chosenCard = player.playCard(trump, suitLead);
            trickCards[playerIdx] = chosenCard;

            if (playerIdx == leaderIdx) {
                suitLead = Optional.of(chosenCard.getEffectiveSuit(trump));
            }
            System.out.println(player.getName() + " played " + chosenCard);
        }

        int trickWinnerIdx = 0;
        int maxPriority = -1;
        for (int i = 0; i < NUM_PLAYERS; i++) {
            int priority = trickCards[i].getPriority(trump, suitLead);
            if (priority > maxPriority) {
                maxPriority = priority;
                trickWinnerIdx = i;
            }
        }
        return trickWinnerIdx;
    }

    private void selectCallerAndTrump() {
        Card upCard = deck.draw();

        System.out.println(upCard + " is the up card");
        for (int offset = 1; offset <= NUM_PLAYERS; offset++) {
            int playerIdx = (dealerIdx + offset) % NUM_PLAYERS;
            Player player = players[playerIdx];

            if (player.chooseToOrderUp(upCard)) {
                trump = upCard.getSuit();
                callerIdx = playerIdx;
                System.out.println(player.getName() + " ordered up");
                //todo: the caller should get the upcard and be prompted to discard a card
                return;
            }
            System.out.println(player.getName() + " did not order up");

        }

        for (int offset = 1; offset <= NUM_PLAYERS; offset++) {
            int playerIdx = (dealerIdx + offset) % NUM_PLAYERS;
            Player player = players[playerIdx];

            boolean dealerIsStuck = playerIdx == dealerIdx;
            Suit calledSuit = player.chooseToCallTrump(upCard.getSuit(), dealerIsStuck);

            //todo stop players from calling the upcard suit
            if (calledSuit == null) {
                System.out.println(player.getName() + " did not choose a suit");
                continue;
            }
            if (calledSuit == upCard.getSuit()) {
                throw new RuntimeException("You cannot call the same suit as the up card");
            }

            trump = calledSuit;
            callerIdx = playerIdx;
            System.out.println(player.getName() + " chose " + calledSuit);

            return;
        }
    }
}
