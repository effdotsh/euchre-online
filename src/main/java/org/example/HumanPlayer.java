package org.example;

import java.util.List;

public class HumanPlayer extends Player {
    public HumanPlayer(String name) {
        super(name);
    }

    @Override
    public Card chooseCard(Suit trump, Suit ledSuit) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        if (legalCards.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }

        return legalCards.getFirst();
    }
}
