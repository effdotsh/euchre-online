import org.example.Card;
import org.example.Deck;
import org.example.Rank;
import org.example.Suit;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {
    @Test
    public void cardStoresSuitAndRank() {
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
    public void rightBowerHasHighestPriority() {
        Suit trump = Suit.SPADES;
        Optional<Suit> lead = Optional.of(Suit.HEARTS);
        Deck deck = Deck.createDeck();
        List<Card> cards = new ArrayList<>(deck.view());
        Collections.sort(cards, (card1, card2) -> card2.getPriority(trump, lead) - card1.getPriority(trump, lead));
        assertEquals(cards.getFirst().getSuit(), trump);
        assertEquals(cards.getFirst().getRank(), Rank.JACK);
    }

    @Test
    public void leftBowerHasSecondHighestPriority() {
        Suit trump = Suit.CLUBS;
        Optional<Suit> lead = Optional.of(Suit.SPADES);
        Deck deck = Deck.createDeck();
        List<Card> cards = new ArrayList<>(deck.view());
        Collections.sort(cards, (card1, card2) -> card2.getPriority(trump, lead) - card1.getPriority(trump, lead));
        int SECOND_POSITION = 1;
        assertNotEquals(trump, cards.get(SECOND_POSITION).getSuit());
        assertEquals(cards.get(SECOND_POSITION).getSuit().getColor(), trump.getColor());
        assertEquals(cards.get(SECOND_POSITION).getRank(), Rank.JACK);
    }

    @Test
    public void aceOfTrumpThirdHighestPriority() {
        Suit trump = Suit.DIAMONDS;
        Optional<Suit> lead = Optional.of(Suit.SPADES);
        Deck deck = Deck.createDeck();
        List<Card> cards = new ArrayList<>(deck.view());
        Collections.sort(cards, (card1, card2) -> card2.getPriority(trump, lead) - card1.getPriority(trump, lead));
        int THIRD_POSITION = 2;
        assertEquals(cards.getFirst().getSuit(), trump);
        assertEquals(cards.get(THIRD_POSITION).getRank(), Rank.ACE);
    }

    @Test
    public void nonLeadNonTrumpCardHasPriorityZero() {
        Card card = new Card(Suit.DIAMONDS, Rank.ACE);
        Suit trump = Suit.SPADES;
        Optional<Suit> lead = Optional.of(Suit.HEARTS);
        assertEquals(card.getPriority(trump, lead), 0);
    }

    @Test
    void trumpCardBeatsSuitLedCard() {
        Card trumpCard = new Card(Suit.CLUBS, Rank.NINE);
        Card leadCard = new Card(Suit.HEARTS, Rank.ACE);
        Suit trump = Suit.CLUBS;
        Optional<Suit> lead = Optional.of(Suit.HEARTS);

        assertTrue(trumpCard.getPriority(trump, lead) > leadCard.getPriority(trump, lead));
    }

    @Test
    void higherTrumpBeatsLowerTrump() {
        Card highTrump = new Card(Suit.CLUBS, Rank.ACE);
        Card lowTrump = new Card(Suit.CLUBS, Rank.NINE);
        Suit trump = Suit.CLUBS;
        Optional<Suit> lead = Optional.of(Suit.HEARTS);

        assertTrue(highTrump.getPriority(trump, lead) > lowTrump.getPriority(trump, lead));
    }

    @Test
    void rightBowerBeatsLeftBower() {
        Card rightBower = new Card(Suit.CLUBS, Rank.JACK);
        Card leftBower = new Card(Suit.SPADES, Rank.JACK);
        Suit trump = Suit.CLUBS;
        Optional<Suit> lead = Optional.of(Suit.HEARTS);

        assertTrue(rightBower.getPriority(trump, lead) > leftBower.getPriority(trump, lead));
    }

    @Test
    void leftBowerBeatsAceOfTrump() {
        Card leftBower = new Card(Suit.SPADES, Rank.JACK);
        Card aceOfTrump = new Card(Suit.CLUBS, Rank.ACE);
        Suit trump = Suit.CLUBS;
        Optional<Suit> lead = Optional.of(Suit.HEARTS);

        assertTrue(leftBower.getPriority(trump, lead) > aceOfTrump.getPriority(trump, lead));
    }

    @Test
    void leftBowerEffectiveSuitIsTrump() {
        Suit trump = Suit.CLUBS;
        Card leftBower = new Card(Suit.SPADES, Rank.JACK);
        assertEquals(Suit.SPADES, leftBower.getSuit());
        assertEquals(trump, leftBower.getEffectiveSuit(trump));
    }


    @Test
    void cardToStringFormatsCorrectly() {
        assertEquals("A♥", new Card(Suit.HEARTS, Rank.ACE).toString());
        assertEquals("10♠", new Card(Suit.SPADES, Rank.TEN).toString());
        assertEquals("J♣", new Card(Suit.CLUBS, Rank.JACK).toString());
    }
}
