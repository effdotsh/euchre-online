package org.example;

import java.util.List;

public class Euchre {
    private List<Player> players;
    private Deck deck;
    private static final int NUM_PLAYERS = 4;

    private int dealerIdx = -1;
    private int leaderIdx;
    private int callerIdx;

    private static int NUM_CARDS_PER_HAND = 5;
    private static int POINTS_TO_WIN = 10;

    private int bluePoints = 0;
    private int redPoints = 0;

    private Suit trump;


    public Euchre(List<Player> players) {
        this.players = players;
    }

    private void resetHand() {
        dealerIdx = (dealerIdx + 1) % NUM_PLAYERS;
        leaderIdx = (dealerIdx + 1) % NUM_PLAYERS;
        deck = Deck.createDeck();
        for (Player player : players) {
            player.setHand(deck.draw(NUM_CARDS_PER_HAND));
        }
    }

    public void playGame() {
        while (!isOver()) {
            playHand();
        }
    }

    private void playHand() {
        resetHand();
        selectPickerAndTrump();
        Player player1 = players.getFirst();
        System.out.println("Player1's first hand is:");
        for (Card card : player1.getHand()) {
            System.out.print(card + " ");
        }
        System.out.println();
        System.exit(0);
    }

    private void selectPickerAndTrump() {

        Card upCard = deck.draw();

        for (int offset = 1; offset < NUM_PLAYERS; offset++) {
            int playerIdx = (dealerIdx + offset) % NUM_PLAYERS;
            Player player = players.get(playerIdx);

            if (player.chooseToOrderUp(upCard)) {
                trump = upCard.getSuit();
                callerIdx = playerIdx;
                return;
            }
        }

        for (int offset = 1; offset < NUM_PLAYERS; offset++) {
            int playerIdx = (dealerIdx + offset) % NUM_PLAYERS;
            Player player = players.get(playerIdx);

            boolean dealerIsStuck = playerIdx == dealerIdx;
            Suit calledSuit = player.chooseToCallTrump(dealerIsStuck);
            if (calledSuit == null) {
                continue;
            }
            trump = calledSuit;
            callerIdx = playerIdx;
            return;
        }
    }

    private boolean isOver() {
        return bluePoints >= POINTS_TO_WIN || redPoints >= POINTS_TO_WIN;
    }

}
