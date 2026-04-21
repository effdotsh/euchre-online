package org.example.strategies;

import org.example.Card;
import org.example.Rank;
import org.example.Suit;
import org.example.UpcardRecipient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConservativeStrategy implements EuchreAIStrategy {

    private static final double SELF_THRESHOLD = 40.0;
    private static final double PARTNER_THRESHOLD = 42.0;
    private static final double OPPONENT_THRESHOLD = 43.0;

    private static final double VOID_BONUS = 3.0;
    private static final double OFFSUIT_ACE_POINTS = 6.0;

    private static final double SELF_UPCARD_MULTIPLIER = 1.0;
    private static final double PARTNER_UPCARD_MULTIPLIER = 0.70;
    private static final double OPPONENT_UPCARD_MULTIPLIER = 0.35;

    private static final int NO_TRUMP_CARDS = 0;
    private static final int NO_SUIT_CARDS = 0;
    private static final int SINGLETON_SUIT_SIZE = 1;

    private static final double RIGHT_BOWER_POINTS = 22.0;
    private static final double LEFT_BOWER_POINTS = 15.0;
    private static final double TRUMP_ACE_POINTS = 12.0;
    private static final double TRUMP_KING_POINTS = 10.0;
    private static final double TRUMP_QUEEN_POINTS = 9.0;
    private static final double TRUMP_TEN_POINTS = 8.0;
    private static final double TRUMP_NINE_POINTS = 7.0;
    private static final double NO_POINTS = 0.0;
    private static final double ALL_SUITS_PENALTY = -3.0;

    @Override
    public boolean shouldOrderUp(Card upCard, List<Card> hand, UpcardRecipient upcardRecipient) {
        Suit trump = upCard.getSuit();
        int trumpCount = (int) countTrumpCards(hand, trump);

        double score = scoreTrumpHolding(hand, trump)
                + scoreOffSuitAces(hand, trump)
                + scoreShortSuits(hand, trump, trumpCount)
                + scoreUpcardRecipientImpact(upCard, hand, trump, upcardRecipient);
        return switch (upcardRecipient) {
            case SELF -> score >= SELF_THRESHOLD;
            case PARTNER -> score >= PARTNER_THRESHOLD;
            case OPPONENT -> score >= OPPONENT_THRESHOLD;
        };
    }

    @Override
    public Optional<Suit> chooseCallTrump(Suit forbiddenSuit, List<Card> hand) {
        return Arrays.stream(Suit.values())
                .filter(suit -> suit != forbiddenSuit)
                .map(suit -> Map.entry(suit, scoreCallTrump(hand, suit)))
                .max(Map.Entry.comparingByValue())
                .filter(bestSuit -> bestSuit.getValue() >= SELF_THRESHOLD)
                .map(Map.Entry::getKey);
    }

    @Override
    public Optional<Suit> mustChooseCallTrump(Suit forbiddenSuit, List<Card> hand) {
        return Arrays.stream(Suit.values())
                .filter(suit -> suit != forbiddenSuit)
                .map(suit -> Map.entry(suit, scoreCallTrump(hand, suit)))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    private double scoreCallTrump(List<Card> hand, Suit trump) {
        int trumpCount = (int) countTrumpCards(hand, trump);
        return scoreTrumpHolding(hand, trump)
                + scoreOffSuitAces(hand, trump)
                + scoreShortSuits(hand, trump, trumpCount);
    }

    private long countTrumpCards(List<Card> hand, Suit trump) {
        return hand.stream().filter(card -> card.getEffectiveSuit(trump) == trump).count();
    }

    private double scoreTrumpHolding(List<Card> hand, Suit trump) {
        return hand.stream()
                .filter(card -> card.getEffectiveSuit(trump) == trump)
                .mapToDouble(card -> trumpCardPoints(card, trump))
                .sum();
    }

    private double scoreOffSuitAces(List<Card> hand, Suit trump) {
        return hand.stream()
                .filter(card -> card.getEffectiveSuit(trump) != trump)
                .filter(card -> card.getRank() == Rank.ACE)
                .count() * OFFSUIT_ACE_POINTS;
    }

    private double scoreShortSuits(List<Card> hand, Suit trump, int trumpCount) {
        if (trumpCount == NO_TRUMP_CARDS) {
            return NO_POINTS;
        }

        double shortSuitScore = NO_POINTS;
        boolean hasAllNonTrumpSuits = true;
        for (Suit suit : Suit.values()) {
            if (suit == trump) {
                continue;
            }

            long count = hand.stream().filter(card -> card.getEffectiveSuit(trump) == suit).count();
            if (count == NO_SUIT_CARDS) {
                hasAllNonTrumpSuits = false;
                shortSuitScore += VOID_BONUS;
            }
        }

        if (hasAllNonTrumpSuits) {
            shortSuitScore += ALL_SUITS_PENALTY;
        }

        return shortSuitScore;
    }

    private double scoreUpcardRecipientImpact(Card upCard,
                                              List<Card> hand,
                                              Suit trump,
                                              UpcardRecipient upcardRecipient) {
        double upcardTrumpPoints = trumpCardPoints(upCard, trump);
        return switch (upcardRecipient) {
            case SELF -> upcardTrumpPoints * SELF_UPCARD_MULTIPLIER
                    + (canShortSuitSelfAfterPickup(hand, trump) ? VOID_BONUS : NO_POINTS);
            case PARTNER -> upcardTrumpPoints * PARTNER_UPCARD_MULTIPLIER;
            case OPPONENT -> -upcardTrumpPoints * OPPONENT_UPCARD_MULTIPLIER;
        };
    }

    private boolean canShortSuitSelfAfterPickup(List<Card> hand, Suit trump) {
        for (Suit suit : Suit.values()) {
            if (suit == trump) {
                continue;
            }

            List<Card> cardsInSuit = hand.stream()
                    .filter(card -> card.getEffectiveSuit(trump) == suit)
                    .toList();
            if (cardsInSuit.size() == SINGLETON_SUIT_SIZE && cardsInSuit.getFirst().getRank() != Rank.ACE) {
                return true;
            }
        }
        return false;
    }

    private double trumpCardPoints(Card card, Suit trump) {
        if (card.getRank() == Rank.JACK && card.getSuit() == trump) {
            return RIGHT_BOWER_POINTS;
        }
        if (card.getRank() == Rank.JACK
                && card.getSuit() != trump
                && card.getSuit().getColor() == trump.getColor()) {
            return LEFT_BOWER_POINTS;
        }

        return switch (card.getRank()) {
            case ACE -> TRUMP_ACE_POINTS;
            case KING -> TRUMP_KING_POINTS;
            case QUEEN -> TRUMP_QUEEN_POINTS;
            case TEN -> TRUMP_TEN_POINTS;
            case NINE -> TRUMP_NINE_POINTS;
            default -> NO_POINTS;
        };
    }
}

