package org.example;

import java.util.Collections;
import java.util.List;

public class Euchre {
    private List<Player> players;
    private List<Card> deck;
    private static final int NUM_PLAYERS = 4;
    private int dealer_idx = 0;
    private int leader_idx;

    private void resetHand() {
        dealer_idx = (dealer_idx + 1) % NUM_PLAYERS;
        leader_idx = (dealer_idx + 1) % NUM_PLAYERS;
        deck = Card.createDeck();
        Collections.shuffle(deck);
    }

}
