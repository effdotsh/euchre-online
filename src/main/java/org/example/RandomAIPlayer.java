package org.example;

import java.util.*;

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
    protected Card chooseCard(Suit trump, Optional<Suit> ledSuit) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        if (legalCards.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }
        return legalCards.get(random.nextInt(legalCards.size()));
    }

    @Override
    public boolean chooseToOrderUp(Card upCard) {
        return random.nextBoolean();
    }

    @Override
    public Optional<Suit> chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
        if (!dealerIsStuck && random.nextBoolean()) {
            return Optional.empty();
        }

        List<Suit> suitOptions = Arrays.stream(Suit.values()).filter(s -> s != forbiddenSuit).toList();
        int suitIdx = random.nextInt(suitOptions.size());
        return Optional.of(suitOptions.get(suitIdx));
    }

}
