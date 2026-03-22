package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Player {
    private final String name;
    private final List<Card> hand;

    protected Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
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


    public abstract Card chooseCard(Suit trump, Suit ledSuit);
}
