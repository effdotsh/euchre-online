package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Player {
    private final String name;
    private List<Card> hand;

    protected Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public void setHand(List<Card> cards) {
        hand = cards;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public List<Card> getLegalCards(Suit trump, Suit ledSuit) {
        if (hand.isEmpty()) {
            return List.of();
        }
        if (ledSuit == null) {
            return List.copyOf(hand);
        }

        List<Card> followSuitCards = hand.stream()
                .filter(card -> card.getEffectiveSuit(trump) == ledSuit)
                .toList();

        return followSuitCards.isEmpty() ? List.copyOf(hand) : followSuitCards;
    }

    public final Card playCard(Suit trump, Suit ledSuit) {
        Card chosenCard = chooseCardToPlay(trump, ledSuit);
        removeCard(chosenCard);
        return chosenCard;
    }


    protected abstract Card chooseCardToPlay(Suit trump, Suit ledSuit);

    public boolean chooseToOrderUp(Card upCard) {
        //todo
        return true;
    }

    public Suit chooseToCallTrump(boolean dealerIsStuck) {
        //todo
        return Suit.SPADES;
    }

    public String getName() {
        return name;
    }
}
