package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private int blueTricks = 0;
    private int redTricks = 0;

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
        System.out.println("Blue team points: " + bluePoints);
        System.out.println("Red team points: " + redPoints);

    }

    private void playHand() {
        resetHand();

        selectCallerAndTrump();

        for (int trickIdx = 0; trickIdx < NUM_CARDS_PER_HAND; trickIdx++) {
            int trickWinnerIdx = playTrick();
            if ((trickWinnerIdx % 2) == 0) {
                blueTricks++;
            } else {
                redTricks++;
            }
            leaderIdx = trickWinnerIdx;
            Player trickWinner = players.get(trickWinnerIdx);
            System.out.println(trickWinner.getName() + " won the trick\n\n");
        }
        scoreHand();
        System.out.println("Blue team points: " + bluePoints);
        System.out.println("Red team points: " + redPoints);
        System.out.println("###########");

    }

    private void scoreHand() {
        //todo: add way to go alone
        boolean blueCalled = (callerIdx % 2) == 0;
        int callingTeamTricks = blueCalled ? blueTricks : redTricks;

        if (callingTeamTricks == 5) {
            addPoints(blueCalled, 2);
        } else if (callingTeamTricks >= 3) {
            addPoints(blueCalled, 1);
        } else {
            addPoints(!blueCalled, 2);
        }
    }

    private void addPoints(boolean blueTeam, int points) {
        if (blueTeam) {
            bluePoints += points;
        } else {
            redPoints += points;
        }
    }

    private int playTrick() {
        List<Card> trickCards = new ArrayList<>(Collections.nCopies(4, null));
        Suit suitLead = null;
        for (int offset = 0; offset < NUM_PLAYERS; offset++) {
            int playerIdx = (leaderIdx + offset) % NUM_PLAYERS;
            Player player = players.get(playerIdx);
            Card chosenCard = player.playCard(trump, suitLead);
            trickCards.set(playerIdx, chosenCard);

            if (playerIdx == leaderIdx) {
                suitLead = chosenCard.getSuit();
            }
            System.out.println(player.getName() + " played " + chosenCard);
        }
        final Suit finalSuitLead = suitLead;
        List<Integer> priorities = trickCards.stream().map(card -> card.getPriority(trump, finalSuitLead)).collect(Collectors.toList());
        int trickWinnerIdx = priorities.indexOf(Collections.max(priorities));
        return trickWinnerIdx;
    }

    private void selectCallerAndTrump() {
        Card upCard = deck.draw();

        System.out.println(upCard + " is the up card");
        for (int offset = 1; offset <= NUM_PLAYERS; offset++) {
            int playerIdx = (dealerIdx + offset) % NUM_PLAYERS;
            Player player = players.get(playerIdx);

            if (player.chooseToOrderUp(upCard)) {
                trump = upCard.getSuit();
                callerIdx = playerIdx;
                System.out.println(player.getName() + " ordered up");
                return;
            }
            System.out.println(player.getName() + " did not order up");

        }

        for (int offset = 1; offset <= NUM_PLAYERS; offset++) {
            int playerIdx = (dealerIdx + offset) % NUM_PLAYERS;
            Player player = players.get(playerIdx);

            boolean dealerIsStuck = playerIdx == dealerIdx;
            Suit calledSuit = player.chooseToCallTrump(dealerIsStuck);
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

    private boolean isOver() {
        return bluePoints >= POINTS_TO_WIN || redPoints >= POINTS_TO_WIN;
    }

}
