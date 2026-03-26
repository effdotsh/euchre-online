package org.example;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomAIPlayer extends Player {
    private final Random random;

    public RandomAIPlayer(String name) {
        this(name, new Random());
    }

    public RandomAIPlayer(String name, Random random) {
        super(name);
        this.random = Objects.requireNonNull(random, "random cannot be null");
    }

    @Override
    protected Card chooseCardToPlay(Suit trump, Suit ledSuit) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        if (legalCards.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }
        return legalCards.get(random.nextInt(legalCards.size()));
    }

    @Override
    public boolean chooseToOrderUp(Card upCard) {
        return (random.nextDouble() > 0.5);
    }

    @Override
    public Suit chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
        if (!dealerIsStuck && random.nextDouble() > 0.5) {
            return null;
        }

        List<Suit> suitOptions = Arrays.stream(Suit.values()).filter(s -> s != forbiddenSuit).toList();
        int suitIdx = random.nextInt(suitOptions.size());
        return suitOptions.get(suitIdx);
    }
    
}
