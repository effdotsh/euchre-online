package org.example.Strategies;

import org.example.Card;
import org.example.Rank;
import org.example.Suit;
import org.example.UpcardRecipient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConservativeStrategy extends EuchreAIStrategy {

    private static final double SELF_THRESHOLD = 40.0;
    private static final double PARTNER_THRESHOLD = 42.0;
    private static final double OPPONENT_THRESHOLD = 43.0;

    private static final double VOID_BONUS = 3.0;
    private static final double OFFSUIT_ACE_POINTS = 6.0;
    private static final double SELF_UPCARD_MULTIPLIER = 1.0;
    private static final double PARTNER_UPCARD_MULTIPLIER = 0.70;
    private static final double OPPONENT_UPCARD_MULTIPLIER = 0.35;
    private static final double ALL_SUITS_PENALTY = -3.0;

    private static final double RIGHT_BOWER_POINTS = 22.0;
    private static final double LEFT_BOWER_POINTS = 15.0;
    private static final double TRUMP_ACE_POINTS = 12.0;
    private static final double TRUMP_KING_POINTS = 10.0;
    private static final double TRUMP_QUEEN_POINTS = 9.0;
    private static final double TRUMP_TEN_POINTS = 8.0;
    private static final double TRUMP_NINE_POINTS = 7.0;

    @Override
    public boolean shouldOrderUp(Card upCard, List<Card> hand, UpcardRecipient upcardRecipient) {
        Suit trump = upCard.getSuit();
        int trumpCount = (int) countTrumpCards(hand, trump);

        double score = scoreTrumpHolding(hand, trump)
                + scoreOffSuitAces(hand, trump, OFFSUIT_ACE_POINTS)
                + scoreShortSuits(hand, trump, trumpCount, VOID_BONUS, ALL_SUITS_PENALTY)
                + scoreUpcardRecipientImpact(upCard, hand, trump, upcardRecipient, SELF_UPCARD_MULTIPLIER, PARTNER_UPCARD_MULTIPLIER, OPPONENT_UPCARD_MULTIPLIER, VOID_BONUS);

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
                .map(suit -> Map.entry(suit, scoreCallTrumpHelper(hand, suit)))
                .max(Map.Entry.comparingByValue())
                .filter(bestSuit -> bestSuit.getValue() >= SELF_THRESHOLD)
                .map(Map.Entry::getKey);
    }

    @Override
    public Optional<Suit> mustChooseCallTrump(Suit forbiddenSuit, List<Card> hand) {
        return Arrays.stream(Suit.values())
                .filter(suit -> suit != forbiddenSuit)
                .map(suit -> Map.entry(suit, scoreCallTrumpHelper(hand, suit)))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    private double scoreCallTrumpHelper(List<Card> hand, Suit trump) {
        int trumpCount = (int) countTrumpCards(hand, trump);
        return scoreTrumpHolding(hand, trump)
                + scoreOffSuitAces(hand, trump, OFFSUIT_ACE_POINTS)
                + scoreShortSuits(hand, trump, trumpCount, VOID_BONUS, ALL_SUITS_PENALTY);
    }

    @Override
    protected double trumpCardPoints(Card card, Suit trump) {
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

