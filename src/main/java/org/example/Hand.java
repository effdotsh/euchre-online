package org.example;


import org.example.Players.Player;

import java.util.*;

import static org.example.Euchre.NUM_PLAYERS;

public class Hand {
    private static final int NUM_CARDS_PER_HAND = 5;

    private final Player[] players;
    private final Deck deck;
    private final long actionDelayMillis;

    private int blueTricks = 0;
    private int redTricks = 0;

    private Card upCard;
    private Suit trump;

    private final int dealerIdx;
    private int leaderIdx;
    private int callerIdx = -1;
    private Bid finalBid;

    private boolean started = false;
    private boolean complete = false;
    private int[] scoredPoints = new int[]{0, 0};
    private final int[] playerTricks = new int[NUM_PLAYERS];
    private final List<Map<String, Object>> completedTricks = new ArrayList<>();
    private Map<String, Object> currentTrick = emptyTrickSnapshot();

    public Hand(Player[] players, int dealerIdx) {
        this(players, dealerIdx, 0);
    }

    public Hand(Player[] players, int dealerIdx, long actionDelayMillis) {
        this.players = players;
        this.dealerIdx = dealerIdx;
        this.leaderIdx = (dealerIdx + 1) % NUM_PLAYERS;
        this.deck = Deck.createDeck();
        this.actionDelayMillis = actionDelayMillis;
    }


    public int[] playHand() {
        start();
        while (!isComplete()) {
            playNextTrick();
        }
        return scoredPoints.clone();
    }

    public void start() {
        if (started) {
            return;
        }
        deal();
        selectCallerAndTrump();
        started = true;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isStarted() {
        return started;
    }

    public void playNextTrick() {
        if (!started) {
            throw new IllegalStateException("Hand must be started before playing tricks");
        }
        if (complete) {
            return;
        }

        int trickWinnerIdx = playTrick();
        if ((trickWinnerIdx % 2) == 0) {
            blueTricks++;
        } else {
            redTricks++;
        }
        playerTricks[trickWinnerIdx]++;
        leaderIdx = trickWinnerIdx;
        Player trickWinner = players[trickWinnerIdx];
        currentTrick.put("winnerIdx", trickWinnerIdx);
        currentTrick.put("winnerName", trickWinner.getName());
        completedTricks.add(currentTrick);
        System.out.println(trickWinner.getName() + " won the trick\n\n");
        pause();

        if ((blueTricks + redTricks) == NUM_CARDS_PER_HAND) {
            scoredPoints = scoreHand();
            complete = true;
        }
    }

    private void deal() {
        for (Player player : players) {
            player.setHand(deck.draw(NUM_CARDS_PER_HAND));
            player.sortHand(Optional.empty());
        }
    }

    private int[] scoreHand() {
        boolean blueCalled = (callerIdx % 2) == 0;
        int callingTeamTricks = blueCalled ? blueTricks : redTricks;

        int callerPoints = 0;
        int defenderPoints = 0;


        if (callingTeamTricks == 5) {
            if (finalBid.isAlone()) {
                callerPoints = 4;
            } else {
                callerPoints = 2;
            }
        } else if (callingTeamTricks >= 3) {
            callerPoints = 1;
        } else {
            defenderPoints = 2;
        }

        if (blueCalled) {
            return new int[]{callerPoints, defenderPoints};
        } else {
            return new int[]{defenderPoints, callerPoints};
        }
    }

    private int playTrick() {
        Card[] trickCards = new Card[NUM_PLAYERS];
        Optional<Suit> suitLead = Optional.empty();
        currentTrick = new LinkedHashMap<>();
        currentTrick.put("leaderIdx", leaderIdx);
        currentTrick.put("ledSuit", null);
        currentTrick.put("plays", new ArrayList<Map<String, Object>>());
        currentTrick.put("winnerIdx", null);
        currentTrick.put("winnerName", null);
        for (int offset = 0; offset < NUM_PLAYERS; offset++) {
            int playerIdx = (leaderIdx + offset) % NUM_PLAYERS;
            Player player = players[playerIdx];
            if (finalBid.isAlone() && playerIdx == (callerIdx + NUM_PLAYERS / 2) % NUM_PLAYERS) {
                System.out.println(player.getName() + " is skipped");
                continue;
            }
            List<Player.PlayedCard> alreadyPlayedCards = buildPlayedCardsView(trickCards, playerIdx);
            Card chosenCard = player.playCard(trump, suitLead, alreadyPlayedCards);
            trickCards[playerIdx] = chosenCard;

            if (suitLead.isEmpty()) {
                suitLead = Optional.of(chosenCard.getEffectiveSuit(trump));
                currentTrick.put("ledSuit", suitLead.get().name());
            }
            Map<String, Object> playSnapshot = new LinkedHashMap<>();
            playSnapshot.put("playerIdx", playerIdx);
            playSnapshot.put("playerName", player.getName());
            playSnapshot.put("card", chosenCard.snapshot(trump));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> plays = (List<Map<String, Object>>) currentTrick.get("plays");
            plays.add(playSnapshot);
            System.out.println(player.getName() + " played " + chosenCard);
            pause();
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

    private List<Player.PlayedCard> buildPlayedCardsView(Card[] trickCards, int perspectivePlayerIdx) {
        List<Player.PlayedCard> playedCards = new ArrayList<>(NUM_PLAYERS - 1);
        for (int idx = 0; idx < trickCards.length; idx++) {
            Card playedCard = trickCards[idx];
            if (playedCard == null) {
                continue;
            }
            playedCards.add(new Player.PlayedCard(
                    playedCard,
                    classifyPlayedBy(perspectivePlayerIdx, idx),
                    idx
            ));
        }
        return List.copyOf(playedCards);
    }

    private Player.PlayedBy classifyPlayedBy(int perspectivePlayerIdx, int playedByPlayerIdx) {
        if (perspectivePlayerIdx == playedByPlayerIdx) {
            return Player.PlayedBy.SELF;
        }
        boolean sameTeam = (perspectivePlayerIdx % 2) == (playedByPlayerIdx % 2);
        return sameTeam ? Player.PlayedBy.PARTNER : Player.PlayedBy.OPPONENT;
    }

    private void selectCallerAndTrump() {
        Optional<Card> upCard = firstRoundSelectCallerAndTrump();

        upCard.ifPresent(this::secondRoundSelectCallerAndTrump);
        for (Player player : players) {
            player.sortHand(Optional.of(trump));
        }
    }

    private void secondRoundSelectCallerAndTrump(Card upCard) {
        for (int offset = 1; offset <= NUM_PLAYERS; offset++) {
            int playerIdx = (dealerIdx + offset) % NUM_PLAYERS;
            Player player = players[playerIdx];

            boolean dealerIsStuck = playerIdx == dealerIdx;
            Bid bid = player.chooseToCallTrump(upCard.getSuit(), dealerIsStuck);


            if (bid.getType() == Bid.BidType.PASS || bid.getTrump().isEmpty()) {
                System.out.println(player.getName() + " did not choose a suit");
                pause();
                continue;
            }

            Suit calledTrump = bid.getTrump().get();

            if (calledTrump == upCard.getSuit()) {
                throw new RuntimeException("You cannot call the same suit as the up card");
            }

            trump = calledTrump;
            callerIdx = playerIdx;
            System.out.println(player.getName() + " chose " + calledTrump);
            if (bid.isAlone()) {
                System.out.println(player.getName() + " is going alone");
            }
            pause();
            finalBid = bid;
            return;
        }
    }

    private Optional<Card> firstRoundSelectCallerAndTrump() {
        upCard = deck.draw();

        System.out.println(upCard + " is the up card");
        pause();
        for (int offset = 1; offset <= NUM_PLAYERS; offset++) {
            int playerIdx = (dealerIdx + offset) % NUM_PLAYERS;
            Player player = players[playerIdx];
            UpcardRecipient upcardRecipient = getUpcardRecipient(playerIdx);

            Bid playerBid = player.chooseToOrderUp(upCard, upcardRecipient);
            if (playerBid.getType() != Bid.BidType.PASS) {
                Player dealer = players[dealerIdx];
                dealer.addCard(upCard);
                Card dealerDiscardedCard = dealer.chooseDiscard(upCard.getSuit(), Optional.empty(), List.of());
                trump = upCard.getSuit();
                callerIdx = playerIdx;
                System.out.println(player.getName() + " ordered up");
                dealer.removeCard(dealerDiscardedCard);
                finalBid = playerBid;
                pause();
                return Optional.empty();
            }
            System.out.println(player.getName() + " did not order up");
            pause();

        }
        return Optional.of(upCard);
    }

    private UpcardRecipient getUpcardRecipient(int playerIdx) {
        if (playerIdx == dealerIdx) {
            return UpcardRecipient.SELF;
        }
        return (playerIdx % 2) == (dealerIdx % 2) ? UpcardRecipient.PARTNER : UpcardRecipient.OPPONENT;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("started", started);
        snapshot.put("complete", complete);
        snapshot.put("dealerIdx", dealerIdx);
        snapshot.put("dealerName", players[dealerIdx].getName());
        snapshot.put("leaderIdx", leaderIdx);
        snapshot.put("leaderName", players[leaderIdx].getName());
        snapshot.put("callerIdx", callerIdx);
        snapshot.put("callerName", callerIdx >= 0 ? players[callerIdx].getName() : null);
        snapshot.put("trump", trump == null ? null : trump.name());
        snapshot.put("upCard", upCard == null ? null : upCard.snapshot(trump));
        snapshot.put("blueTricks", blueTricks);
        snapshot.put("redTricks", redTricks);
        snapshot.put("scoredPoints", List.of(scoredPoints[0], scoredPoints[1]));
        snapshot.put("deck", deck.snapshot(trump));
        snapshot.put("players", snapshotPlayers());
        snapshot.put("currentTrick", currentTrick);
        snapshot.put("completedTricks", List.copyOf(completedTricks));
        return snapshot;
    }

    public Suit getTrump() {
        return trump;
    }

    public int[] getScoredPoints() {
        return scoredPoints.clone();
    }

    private Map<String, Object> emptyTrickSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("leaderIdx", leaderIdx);
        snapshot.put("ledSuit", null);
        snapshot.put("plays", List.of());
        snapshot.put("winnerIdx", null);
        snapshot.put("winnerName", null);
        return snapshot;
    }

    private List<Map<String, Object>> snapshotPlayers() {
        List<Map<String, Object>> snapshots = new ArrayList<>(NUM_PLAYERS);
        for (int i = 0; i < NUM_PLAYERS; i++) {
            Map<String, Object> playerSnapshot = new LinkedHashMap<>(players[i].snapshot(trump));
            playerSnapshot.put("playerIdx", i);
            playerSnapshot.put("trickCount", playerTricks[i]);
            snapshots.add(playerSnapshot);
        }
        return snapshots;
    }

    private void pause() {
        if (actionDelayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(actionDelayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Hand pacing interrupted", e);
        }
    }
}
