package org.example.Players;

import org.example.Bid;
import org.example.Card;
import org.example.Suit;
import org.example.UpcardRecipient;

import java.util.*;

public abstract class Player {
    private final String name;
    private List<Card> hand;


    public Card chooseDiscard(Suit trump, Optional<Suit> ledSuit, List<PlayedCard> playedCards) {
        return chooseCard(trump, ledSuit, List.of());
    }

    public enum PlayedBy {
        PARTNER,
        OPPONENT,
        SELF
    }

    protected Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public void setHand(List<Card> cards) {
        hand = cards;
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public List<Card> getLegalCards(Suit trump, Optional<Suit> ledSuit) {
        if (hand.isEmpty()) {
            return List.of();
        }
        if (ledSuit.isEmpty()) {
            return List.copyOf(hand);
        }

        List<Card> followSuitCards = hand.stream()
                .filter(card -> card.getEffectiveSuit(trump) == ledSuit.get())
                .toList();

        return followSuitCards.isEmpty() ? List.copyOf(hand) : followSuitCards;
    }

    public final Card playCard(Suit trump, Optional<Suit> ledSuit, List<PlayedCard> playedCardsView) {
        Card chosenCard = chooseCard(trump, ledSuit, playedCardsView);
        removeCard(chosenCard);
        return chosenCard;
    }

    protected abstract Card chooseCard(Suit trump, Optional<Suit> ledSuit, List<PlayedCard> playedCards);


    public record PlayedCard(Card card, PlayedBy playedBy, int playerIdx) {
        public PlayedCard {
            Objects.requireNonNull(card, "card cannot be null");
            Objects.requireNonNull(playedBy, "playedBy cannot be null");
        }
    }

    public abstract Bid chooseToOrderUp(Card upCard);

    public Bid chooseToOrderUp(Card upCard, UpcardRecipient upcardRecipient) {
        return chooseToOrderUp(upCard);
    }

    public abstract Bid chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck);

    public void updateScoreContext(int ownTeamPoints, int opposingTeamPoints) {
        // Default do nothing, only for Strategy AI players.
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> snapshot(Suit trump) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("name", name);
        snapshot.put("type", getClass().getSimpleName());
        snapshot.put("handSize", hand.size());
        snapshot.put("hand", hand.stream().map(card -> card.snapshot(trump)).toList());
        return snapshot;
    }

    public void sortHand(Optional<Suit> trump) {
        hand.sort((a, b) -> {
            if (trump.isPresent()) {
                Suit chosenTrump = trump.get();

                int aPriority = a.getPriority(chosenTrump, Optional.of(chosenTrump));
                int bPriority = b.getPriority(chosenTrump, Optional.of(chosenTrump));
                int priorityDelta = bPriority - aPriority;

                if (priorityDelta > 0) {
                    return 1;
                } else if (priorityDelta < 0) {
                    return -1;
                }
            }

            return b.getOrder() - a.getOrder();
        });
    }
}
