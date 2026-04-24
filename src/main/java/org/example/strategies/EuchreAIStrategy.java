package org.example.strategies;

import org.example.Card;
import org.example.Rank;
import org.example.Suit;
import org.example.UpcardRecipient;

import java.util.List;
import java.util.Optional;

public abstract class EuchreAIStrategy {

    private static final int NO_TRUMP_CARDS = 0;
    private static final int NO_SUIT_CARDS = 0;
    private static final int SINGLETON_SUIT_SIZE = 1;
    protected static final double NO_POINTS = 0.0;

    public abstract boolean shouldOrderUp(Card upCard, List<Card> hand, UpcardRecipient upcardRecipient);

    public abstract Optional<Suit> chooseCallTrump(Suit forbiddenSuit, List<Card> hand);

    public abstract Optional<Suit> mustChooseCallTrump(Suit forbiddenSuit, List<Card> hand);

    protected abstract double trumpCardPoints(Card card, Suit trump);


    protected final long countTrumpCards(List<Card> hand, Suit trump) {
        return hand.stream().filter(card -> card.getEffectiveSuit(trump) == trump).count();
    }

    protected final double scoreTrumpHolding(List<Card> hand, Suit trump) {
        return hand.stream()
                .filter(card -> card.getEffectiveSuit(trump) == trump)
                .mapToDouble(card -> trumpCardPoints(card, trump))
                .sum();
    }

    protected final double scoreOffSuitAces(List<Card> hand, Suit trump, double offSuitAcePoints) {
        return hand.stream()
                .filter(card -> card.getEffectiveSuit(trump) != trump)
                .filter(card -> card.getRank() == Rank.ACE)
                .count() * offSuitAcePoints;
    }

    protected final double scoreShortSuits(List<Card> hand, Suit trump, int trumpCount, double voidBonus, double allSuitsPenalty) {
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
                shortSuitScore += voidBonus;
            }
        }

        if (hasAllNonTrumpSuits) {
            shortSuitScore += allSuitsPenalty;
        }

        return shortSuitScore;
    }

    protected final double scoreUpcardRecipientImpact(Card upCard,
                                                      List<Card> hand,
                                                      Suit trump,
                                                      UpcardRecipient upcardRecipient,
                                                      double selfMultiplier,
                                                      double partnerMultiplier,
                                                      double opponentMultiplier,
                                                      double voidBonus) {
        double upcardTrumpPoints = trumpCardPoints(upCard, trump);
        return switch (upcardRecipient) {
            case SELF -> upcardTrumpPoints * selfMultiplier
                    + (canShortSuitSelfAfterPickup(hand, trump) ? voidBonus : NO_POINTS);
            case PARTNER -> upcardTrumpPoints * partnerMultiplier;
            case OPPONENT -> -upcardTrumpPoints * opponentMultiplier;
        };
    }

    protected final boolean canShortSuitSelfAfterPickup(List<Card> hand, Suit trump) {
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

    protected final boolean hasTwoJacks(List<Card> hand, Suit trump) {
        boolean hasRightBower = hand.stream()
                .anyMatch(card -> card.getRank() == Rank.JACK && card.getSuit() == trump);

        boolean hasLeftBower = hand.stream()
                .anyMatch(card -> card.getRank() == Rank.JACK
                        && card.getSuit() != trump
                        && card.getSuit().getColor() == trump.getColor());

        return hasRightBower && hasLeftBower;
    }

}

