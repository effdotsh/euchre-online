package org.example.Players;

import org.example.Card;
import org.example.Rank;
import org.example.Suit;
import org.example.UpcardRecipient;
import org.example.strategies.*;

import java.util.*;
import java.util.stream.Collectors;

public class StrategyAIPlayer extends Player {
    private final Random random;
    private EuchreAIStrategy strategy;
    private final int thresholdForAgressive = -3;
    private final int thresholdForConservative = 3;

    public StrategyAIPlayer(String name, EuchreAIStrategy strategy) {
        this(name, strategy, new Random());
    }

    public StrategyAIPlayer(String name, EuchreAIStrategy strategy, Random random) {
        super(name);
        this.strategy = Objects.requireNonNull(strategy, "strategy cannot be null");
        this.random = Objects.requireNonNull(random, "random cannot be null");
    }

    @Override
    protected Card chooseCard(Suit trump, Optional<Suit> ledSuit, List<PlayedCard> playedCards) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        if (legalCards.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }
        return chooseCardToPlay(trump, ledSuit, playedCards, legalCards);
    }

    @Override
    public Card chooseDiscard(Suit trump, Optional<Suit> ledSuit, List<PlayedCard> playedCards) {
        Card safeDiscard = findLowestSafeDiscard(trump, getHand());
        if (safeDiscard != null) {
            return safeDiscard;
        }

        return findLowestLossCard(trump, getHand());
    }

    @Override
    public void updateScoreContext(int ownTeamPoints, int opposingTeamPoints) {
        int scoreDiff = ownTeamPoints - opposingTeamPoints;
        if (scoreDiff < thresholdForAgressive && !(strategy instanceof AggressiveStrategy)) {
            strategy = StrategyFactory.create(StrategyType.AGGRESSIVE);
        } else if (scoreDiff > thresholdForConservative && !(strategy instanceof ConservativeStrategy)) {
            strategy = StrategyFactory.create(StrategyType.CONSERVATIVE);
        } else if (!(strategy instanceof NeutralStrategy)) {
            strategy = StrategyFactory.create(StrategyType.NEUTRAL);
        }
    }

    private Card chooseCardToPlay(Suit trump,
                                  Optional<Suit> ledSuit,
                                  List<PlayedCard> playedCards,
                                  List<Card> legalCards) {
        Card leadCard = chooseLeadCard(trump, ledSuit, legalCards);
        if (leadCard != null) {
            return leadCard;
        }

        PlayedCard currentWinningPlay = findCurrentWinningPlay(trump, ledSuit, playedCards);
        if (currentWinningPlay != null) {
            if (currentWinningPlay.playedBy() == PlayedBy.OPPONENT
                    && canBeatWithTrump(trump, ledSuit, legalCards, currentWinningPlay)) {
                Card lowestWinningTrump = findLowestWinningTrump(trump, ledSuit, legalCards, currentWinningPlay);
                if (lowestWinningTrump != null) {
                    return lowestWinningTrump;
                }
            }

            if (currentWinningPlay.playedBy() == PlayedBy.PARTNER
                    && isTrumpOrAce(trump, currentWinningPlay.card())) {
                Card safeDiscard = findLowestSafeDiscard(trump, legalCards);
                if (safeDiscard != null) {
                    return safeDiscard;
                }
            }

            if (!canBeatCurrentWinner(trump, ledSuit, legalCards, currentWinningPlay)) {
                return findLowestLossCard(trump, legalCards);
            }
        }

        if (isFollowingNonTrumpSuit(trump, ledSuit, legalCards)) {
            if (partnerPlayedAce(playedCards)) {
                return lowestCard(legalCards);
            }
            return highestCard(legalCards);
        }

        Card fallbackCard = chooseFallbackCard(trump, legalCards);
        if (fallbackCard != null) {
            return fallbackCard;
        }

        return legalCards.get(random.nextInt(legalCards.size()));
    }

    private Card chooseLeadCard(Suit trump, Optional<Suit> ledSuit, List<Card> legalCards) {
        if (ledSuit.isPresent()) {
            return null;
        }

        Card rightBower = findRightBower(trump, legalCards);
        if (rightBower != null) {
            return rightBower;
        }

        Card shortSuitDiscard = findShortSuitDiscardWhenBackedByTrump(trump, legalCards);
        if (shortSuitDiscard != null) {
            return shortSuitDiscard;
        }

        Card nonTrumpHighest = findHighestNonTrump(trump, legalCards);
        if (nonTrumpHighest != null) {
            return nonTrumpHighest;
        }

        return legalCards.getFirst();
    }

    private Card chooseFallbackCard(Suit trump, List<Card> legalCards) {
        Card shortSuitDiscard = findShortSuitDiscardWhenBackedByTrump(trump, legalCards);
        if (shortSuitDiscard != null) {
            return shortSuitDiscard;
        }

        Card nonTrumpAce = findHighestNonTrumpAce(trump, legalCards);
        if (nonTrumpAce != null) {
            return nonTrumpAce;
        }

        Card highestNonTrumpPlay = findHighestNonTrumpPlay(trump, legalCards);
        if (highestNonTrumpPlay != null) {
            return highestNonTrumpPlay;
        }

        return null;
    }

    private boolean isFollowingNonTrumpSuit(Suit trump, Optional<Suit> ledSuit, List<Card> legalCards) {
        return ledSuit.isPresent()
                && ledSuit.get() != trump
                && !legalCards.isEmpty()
                && legalCards.stream().allMatch(card -> card.getEffectiveSuit(trump) == ledSuit.get());
    }

    private boolean partnerPlayedAce(List<PlayedCard> playedCards) {
        return playedCards.stream()
                .anyMatch(playedCard -> playedCard.playedBy() == PlayedBy.PARTNER
                        && playedCard.card().getRank() == Rank.ACE);
    }

    private PlayedCard findCurrentWinningPlay(Suit trump,
                                              Optional<Suit> ledSuit,
                                              List<PlayedCard> playedCards) {
        return playedCards.stream()
                .max(Comparator.comparingInt((PlayedCard playedCard) -> playedCard.card().getPriority(trump, ledSuit)))
                .orElse(null);
    }

    private boolean canBeatWithTrump(Suit trump,
                                     Optional<Suit> ledSuit,
                                     List<Card> legalCards,
                                     PlayedCard currentWinningPlay) {
        return legalCards.stream()
                .anyMatch(card -> card.getEffectiveSuit(trump) == trump
                        && card.getPriority(trump, ledSuit) > currentWinningPlay.card().getPriority(trump, ledSuit));
    }

    private Card findLowestWinningTrump(Suit trump,
                                        Optional<Suit> ledSuit,
                                        List<Card> legalCards,
                                        PlayedCard currentWinningPlay) {
        return legalCards.stream()
                .filter(card -> card.getEffectiveSuit(trump) == trump)
                .filter(card -> card.getPriority(trump, ledSuit) > currentWinningPlay.card().getPriority(trump, ledSuit))
                .min(Comparator.comparingInt(card -> card.getPriority(trump, ledSuit)))
                .orElse(null);
    }

    private boolean canBeatCurrentWinner(Suit trump,
                                         Optional<Suit> ledSuit,
                                         List<Card> legalCards,
                                         PlayedCard currentWinningPlay) {
        int winningPriority = currentWinningPlay.card().getPriority(trump, ledSuit);
        return legalCards.stream().anyMatch(card -> card.getPriority(trump, ledSuit) > winningPriority);
    }

    private Card findLowestLossCard(Suit trump, List<Card> legalCards) {
        return legalCards.stream()
                .filter(card -> card.getEffectiveSuit(trump) != trump)
                .min(Comparator.comparingInt((Card card) -> card.getRank().getValue())
                        .thenComparingInt(Card::getOrder))
                .orElseGet(() -> lowestCard(legalCards));
    }

    private boolean isTrumpOrAce(Suit trump, Card card) {
        return card.getEffectiveSuit(trump) == trump || card.getRank() == Rank.ACE;
    }

    private Card findLowestSafeDiscard(Suit trump, List<Card> legalCards) {
        Map<Suit, Long> nonTrumpCounts = legalCards.stream()
                .filter(card -> card.getEffectiveSuit(trump) != trump)
                .collect(Collectors.groupingBy(card -> card.getEffectiveSuit(trump), Collectors.counting()));

        return legalCards.stream()
                .filter(card -> card.getEffectiveSuit(trump) != trump)
                .filter(card -> card.getRank() != Rank.ACE)
                .min(Comparator.comparingLong((Card card) -> nonTrumpCounts.getOrDefault(card.getEffectiveSuit(trump), Long.MAX_VALUE))
                        .thenComparingInt(card -> card.getRank().getValue())
                        .thenComparingInt(Card::getOrder))
                .orElse(null);
    }

    private Card findShortSuitDiscardWhenBackedByTrump(Suit trump, List<Card> legalCards) {
        boolean hasTrump = legalCards.stream().anyMatch(card -> card.getEffectiveSuit(trump) == trump);
        if (!hasTrump) {
            return null;
        }

        return findLowestSafeDiscard(trump, legalCards);
    }

    private Card findRightBower(Suit trump, List<Card> legalCards) {
        return legalCards.stream()
                .filter(card -> card.getRank() == Rank.JACK && card.getSuit() == trump)
                .findFirst()
                .orElse(null);
    }

    private Card findHighestNonTrumpAce(Suit trump, List<Card> legalCards) {
        return legalCards.stream()
                .filter(card -> card.getRank() == Rank.ACE)
                .filter(card -> card.getEffectiveSuit(trump) != trump)
                .max(Comparator.comparingInt(Card::getOrder))
                .orElse(null);
    }

    private Card findHighestNonTrump(Suit trump, List<Card> legalCards) {
        return legalCards.stream()
                .filter(card -> card.getEffectiveSuit(trump) != trump)
                .max(Comparator.comparingInt(Card::getOrder))
                .orElse(null);
    }

    private Card findHighestNonTrumpPlay(Suit trump, List<Card> legalCards) {
        return legalCards.stream()
                .filter(card -> card.getEffectiveSuit(trump) != trump)
                .max(Comparator.comparingInt(Card::getOrder))
                .orElse(null);
    }

    private Card lowestCard(List<Card> cards) {
        return cards.stream()
                .min(Comparator.comparingInt(Card::getOrder))
                .orElseThrow(() -> new IllegalStateException("No legal cards available"));
    }

    private Card highestCard(List<Card> cards) {
        return cards.stream()
                .max(Comparator.comparingInt(Card::getOrder))
                .orElseThrow(() -> new IllegalStateException("No legal cards available"));
    }

    @Override
    public boolean chooseToOrderUp(Card upCard) {
        return chooseToOrderUp(upCard, UpcardRecipient.OPPONENT);
    }

    @Override
    public boolean chooseToOrderUp(Card upCard, UpcardRecipient upcardRecipient) {
        return strategy.shouldOrderUp(upCard, getHand(), upcardRecipient);
    }

    @Override
    public Optional<Suit> chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
        if (dealerIsStuck) {
            return strategy.mustChooseCallTrump(forbiddenSuit, getHand());
        }

        return strategy.chooseCallTrump(forbiddenSuit, getHand());
    }

}

