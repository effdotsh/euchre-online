package org.example;

import java.util.ArrayList;
import java.util.List;

public class Euchre {
    private Player[] players;

    public static final int NUM_PLAYERS = 4;

    private int dealerIdx = -1;

    private static final int POINTS_TO_WIN = 10;

    private int bluePoints = 0;
    private int redPoints = 0;

    List<Hand> hands = new ArrayList<>();

    public Euchre(Player[] players) {
        this.players = players;
    }

    public void playGame() {
        while (!isOver()) {
            dealerIdx = (dealerIdx + 1) % NUM_PLAYERS;
            Hand hand = new Hand(players, dealerIdx);
            hands.add(hand);

            int[] points = hand.playHand();
            bluePoints += points[0];
            redPoints += points[1];

            System.out.println("Blue team points: " + bluePoints);
            System.out.println("Red team points: " + redPoints);
            System.out.println("###########");
        }
        System.out.println("Blue team points: " + bluePoints);
        System.out.println("Red team points: " + redPoints);
    }

    private boolean isOver() {
        return bluePoints >= POINTS_TO_WIN || redPoints >= POINTS_TO_WIN;
    }
}
