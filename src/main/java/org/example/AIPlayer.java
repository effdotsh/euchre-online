package org.example;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AIPlayer extends Player {
    private final Random random;

    public AIPlayer(String name) {
        this(name, new Random());
    }

    public AIPlayer(String name, Random random) {
        super(name);
        this.random = Objects.requireNonNull(random, "random cannot be null");
    }

    public Card chooseCard(Suit ledSuit, Suit trump) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        if (legalCards.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }
        return legalCards.get(random.nextInt(legalCards.size()));
    }
}
