package org.example.Players;

import org.example.Bid;
import org.example.Card;
import org.example.Suit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RemotePlayer extends Player {
    private PendingAction pendingAction;
    private String submittedValue;

    public RemotePlayer(String name) {
        super(name);
    }

    @Override
    protected synchronized Card chooseCard(Suit trump, Optional<Suit> ledSuit, List<PlayedCard> playedCards) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        pendingAction = PendingAction.playCard(legalCards, ledSuit);
        String cardId = awaitSubmission();
        return legalCards.stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Submitted card is not legal"));
    }

    @Override
    public synchronized Bid chooseToOrderUp(Card upCard) {
        pendingAction = PendingAction.orderUp(getHand(), upCard);
        String submission = awaitSubmission();
        if (PendingAction.PASS.equals(submission)) {
            return Bid.pass();
        }
        return Bid.orderUp(false);
    }

    @Override
    public synchronized Bid chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
        pendingAction = PendingAction.callTrump(forbiddenSuit, dealerIsStuck);
        String submission = awaitSubmission();
        if (PendingAction.PASS.equals(submission)) {
            return Bid.pass();
        }
        return Bid.callTrump(Suit.valueOf(submission), false);
    }

    public synchronized void submit(String value) {
        if (pendingAction == null) {
            throw new IllegalStateException("No pending action");
        }
        if (!pendingAction.isAllowed(value)) {
            throw new IllegalArgumentException("Submitted action is not allowed");
        }
        submittedValue = value;
        notifyAll();
    }

    @Override
    public synchronized Map<String, Object> snapshot(Suit trump) {
        Map<String, Object> snapshot = super.snapshot(trump);
        snapshot.put("isRemote", true);
        snapshot.put("pendingAction", pendingAction == null ? null : pendingAction.snapshot());
        return snapshot;
    }

    private synchronized String awaitSubmission() {
        submittedValue = null;
        notifyAll();
        while (submittedValue == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Remote player interrupted", e);
            }
        }
        String value = submittedValue;
        submittedValue = null;
        pendingAction = null;
        return value;
    }

    private record PendingAction(
            String type,
            List<String> allowedValues,
            List<Map<String, Object>> cards,
            List<String> suits,
            boolean canPass,
            Map<String, Object> upCard,
            String ledSuit
    ) {
        private static final String PASS = "PASS";
        private static final String ORDER_UP = "ORDER_UP";

        private static PendingAction playCard(List<Card> legalCards, Optional<Suit> ledSuit) {
            return new PendingAction(
                    "play_card",
                    legalCards.stream().map(Card::getId).toList(),
                    legalCards.stream().map(card -> card.snapshot(null)).toList(),
                    List.of(),
                    false,
                    null,
                    ledSuit.map(Enum::name).orElse(null)
            );
        }

        private static PendingAction orderUp(List<Card> hand, Card upCard) {
            return new PendingAction(
                    "order_up",
                    List.of(ORDER_UP, PASS),
                    List.of(),
                    List.of(),
                    true,
                    upCard.snapshot(upCard.getSuit()),
                    null
            );
        }

        private static PendingAction callTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
            List<String> suits = java.util.Arrays.stream(Suit.values())
                    .filter(suit -> suit != forbiddenSuit)
                    .map(Enum::name)
                    .toList();
            return new PendingAction(
                    "call_trump",
                    buildAllowedValues(suits, !dealerIsStuck),
                    List.of(),
                    suits,
                    !dealerIsStuck,
                    null,
                    null
            );
        }

        private static List<String> buildAllowedValues(List<String> values, boolean canPass) {
            if (!canPass) {
                return values;
            }
            List<String> allowedValues = new java.util.ArrayList<>(values);
            allowedValues.add(PASS);
            return List.copyOf(allowedValues);
        }

        private boolean isAllowed(String value) {
            return allowedValues.contains(value);
        }

        private Map<String, Object> snapshot() {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("type", type);
            snapshot.put("cards", cards);
            snapshot.put("suits", suits);
            snapshot.put("canPass", canPass);
            snapshot.put("upCard", upCard);
            snapshot.put("ledSuit", ledSuit);
            snapshot.put("passValue", PASS);
            snapshot.put("orderUpValue", ORDER_UP);
            return snapshot;
        }
    }
}
