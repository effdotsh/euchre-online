package org.example.Players;

import org.example.Bid;
import org.example.Card;
import org.example.Suit;

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
    protected Card chooseCard(Suit trump, Optional<Suit> ledSuit, List<PlayedCard> playedCards) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        if (legalCards.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }
        return legalCards.get(random.nextInt(legalCards.size()));
    }

    @Override
    public Bid chooseToOrderUp(Card upCard) {
        return random.nextBoolean() ? Bid.orderUp(false) : Bid.pass();
    }

    @Override
    public Bid chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
        if (!dealerIsStuck && random.nextBoolean()) {
            return Bid.pass();
        }

        List<Suit> suitOptions = Arrays.stream(Suit.values()).filter(s -> s != forbiddenSuit).toList();
        int suitIdx = random.nextInt(suitOptions.size());
        return Bid.callTrump(suitOptions.get(suitIdx), false);
    }

}
