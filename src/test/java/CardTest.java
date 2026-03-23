import org.example.Card;
import org.example.Deck;
import org.example.Rank;
import org.example.Suit;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CardTest {
    @Test
    public void testCardStoresSuitAndRank() {
        Card card1 = new Card(Suit.HEARTS, Rank.ACE);
        assertEquals(card1.getSuit(), Suit.HEARTS);
        assertEquals(card1.getRank(), Rank.ACE);
        assertNotEquals(Suit.DIAMONDS, card1.getSuit());
        assertNotEquals(Rank.JACK, card1.getRank());

        Card card2 = new Card(Suit.SPADES, Rank.JACK);
        assertEquals(card2.getSuit(), Suit.SPADES);
        assertEquals(card2.getRank(), Rank.JACK);

        Card card3 = new Card(Suit.DIAMONDS, Rank.NINE);
        assertEquals(card3.getSuit(), Suit.DIAMONDS);
        assertEquals(card3.getRank(), Rank.NINE);
    }

    @Test
    public void testRightBowerHasHighestPriority() {
        Suit trump = Suit.SPADES;
        Suit lead = Suit.HEARTS;
        Deck deck = Deck.createDeck();
        List<Card> cards = new ArrayList<>(deck.view());
        Collections.sort(cards, (card1, card2) -> card2.getPriority(trump, lead) - card1.getPriority(trump, lead));
        assertEquals(cards.getFirst().getSuit(), trump);
        assertEquals(cards.getFirst().getRank(), Rank.JACK);
    }

    @Test
    public void testLeftBowerHasSecondHighestPriority() {
        Suit trump = Suit.CLUBS;
        Suit lead = Suit.SPADES;
        Deck deck = Deck.createDeck();
        List<Card> cards = new ArrayList<>(deck.view());
        Collections.sort(cards, (card1, card2) -> card2.getPriority(trump, lead) - card1.getPriority(trump, lead));
        int SECOND_POSITION = 1;
        assertNotEquals(trump, cards.get(SECOND_POSITION).getSuit());
        assertEquals(cards.get(SECOND_POSITION).getSuit().getColor(), trump.getColor());
        assertEquals(cards.get(SECOND_POSITION).getRank(), Rank.JACK);
    }

    @Test
    public void testAceOfTrumpThirdHighestPriority() {
        Suit trump = Suit.DIAMONDS;
        Suit lead = Suit.SPADES;
        Deck deck = Deck.createDeck();
        List<Card> cards = new ArrayList<>(deck.view());
        Collections.sort(cards, (card1, card2) -> card2.getPriority(trump, lead) - card1.getPriority(trump, lead));
        int THIRD_POSITION = 2;
        assertEquals(cards.getFirst().getSuit(), trump);
        assertEquals(cards.get(THIRD_POSITION).getRank(), Rank.ACE);
    }

    @Test
    public void testNonLeadNonTrumpCardHasPriorityZero() {
        Card card = new Card(Suit.DIAMONDS, Rank.ACE);
        Suit trump = Suit.SPADES;
        Suit lead = Suit.HEARTS;
        assertEquals(card.getPriority(trump, lead), 0);
    }
}
