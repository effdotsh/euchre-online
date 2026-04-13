package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Deck {
    List<Card> cards;

    private Deck(List<Card> cards) {
        this.cards = cards;
    }

    public static Deck createDeck() {
        List<Card> cards = new ArrayList<>();

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);

        return new Deck(cards);
    }

    public List<Card> view() {
        return Collections.unmodifiableList(cards);
    }

    public List<Card> draw(int numCards) {
        List<Card> drawnCards = new ArrayList<>(
                cards.subList(cards.size() - numCards, cards.size())
        );
        cards.subList(cards.size() - numCards, cards.size()).clear();
        return drawnCards;
    }

    public Card draw() {
        int SINGLE_CARD = 1;
        return draw(SINGLE_CARD).getFirst();
    }

    public Map<String, Object> snapshot(Suit trump) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("remaining", cards.size());
        snapshot.put("cards", cards.stream().map(card -> card.snapshot(trump)).toList());
        return snapshot;
    }
}
