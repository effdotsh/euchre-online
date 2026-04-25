package org.example;

import org.example.Players.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Euchre {
    private final Player[] players;
    private final long actionDelayMillis;
    private final boolean stickDealer;

    public static final int NUM_PLAYERS = 4;

    private int dealerIdx = -1;

    private static final int POINTS_TO_WIN = 10;

    private int bluePoints = 0;
    private int redPoints = 0;
    private int scoredHandCount = 0;

    List<Hand> hands = new ArrayList<>();

    public Euchre(Player[] players) {
        this(players, 0, true);
    }

    public Euchre(Player[] players, long actionDelayMillis, boolean stickDealer) {
        this.players = players;
        this.actionDelayMillis = actionDelayMillis;
        this.stickDealer = stickDealer;
    }

    public void playGame() {
        while (!isOver()) {
            advance();
        }
        System.out.println("Blue team points: " + bluePoints);
        System.out.println("Red team points: " + redPoints);
    }

    public Hand startNextHand() {
        if (isOver()) {
            throw new IllegalStateException("Game is already over");
        }
        if (!hands.isEmpty() && !hands.getLast().isComplete()) {
            throw new IllegalStateException("Current hand must complete before starting the next hand");
        }
        updatePlayersScoreContext();
        dealerIdx = (dealerIdx + 1) % NUM_PLAYERS;
        Hand hand = new Hand(players, dealerIdx, actionDelayMillis, stickDealer);
        hands.add(hand);
        try {
            hand.start();
        } catch (RuntimeException e) {
            hands.removeLast();
            throw e;
        }
        return hand;
    }

    private void updatePlayersScoreContext() {
        for (int playerIdx = 0; playerIdx < players.length; playerIdx++) {
            boolean onBlueTeam = (playerIdx % 2) == 0;
            int ownTeamPoints = onBlueTeam ? bluePoints : redPoints;
            int opposingTeamPoints = onBlueTeam ? redPoints : bluePoints;
            players[playerIdx].updateScoreContext(ownTeamPoints, opposingTeamPoints);
        }
    }

    public void advance() {
        if (isOver()) {
            return;
        }

        if (hands.isEmpty() || scoredHandCount == hands.size()) {
            startNextHand();
            return;
        }

        Hand hand = hands.getLast();
        if (!hand.isComplete()) {
            hand.playNextTrick();
        }

        if (hand.isComplete() && scoredHandCount < hands.size()) {
            int[] points = hand.getScoredPoints();
            bluePoints += points[0];
            redPoints += points[1];
            scoredHandCount++;
            System.out.println("Blue team points: " + bluePoints);
            System.out.println("Red team points: " + redPoints);
            System.out.println("###########");
        }
    }

    public String snapshot() {
        return Json.stringify(snapshotData());
    }

    private Map<String, Object> snapshotData() {
        Suit trump = currentTrump();
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("game", "Euchre");
        snapshot.put("isOver", isOver());
        snapshot.put("dealerIdx", dealerIdx);
        snapshot.put("dealerName", dealerIdx >= 0 ? players[dealerIdx].getName() : null);
        snapshot.put("bluePoints", bluePoints);
        snapshot.put("redPoints", redPoints);
        snapshot.put("players", List.of(players).stream().map(player -> player.snapshot(trump)).toList());
        snapshot.put("currentHand", hands.isEmpty() ? null : hands.getLast().snapshot());
        snapshot.put("handCount", hands.size());
        snapshot.put("hands", hands.stream().map(Hand::snapshot).toList());
        return snapshot;
    }

    private Suit currentTrump() {
        return hands.isEmpty() ? null : hands.getLast().getTrump();
    }

    private boolean isOver() {
        return bluePoints >= POINTS_TO_WIN || redPoints >= POINTS_TO_WIN;
    }

    public boolean isStickDealer() {
        return stickDealer;
    }
}
