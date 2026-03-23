import org.example.Card;
import org.example.Deck;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void deckHas24Cards() {
        Deck deck = Deck.createDeck();
        assertEquals(24, deck.view().size());
    }

    @Test
    void deckHasNoDuplicates() {
        Deck deck = Deck.createDeck();
        List<Card> cards = deck.view();
        Set<String> seen = new HashSet<>();
        for (Card card : cards) {
            assertTrue(seen.add(card.toString()), "Duplicate card: " + card);
        }
    }

    @Test
    void drawRemovesCardsFromDeck() {
        Deck deck = Deck.createDeck();
        List<Card> drawn = deck.draw(5);
        assertEquals(5, drawn.size());
        assertEquals(19, deck.view().size());
    }

    @Test
    void drawSingleCardRemovesOneCard() {
        Deck deck = Deck.createDeck();
        Card card = deck.draw();
        assertNotNull(card);
        assertEquals(23, deck.view().size());
    }

    @Test
    void drawAllCardsEmptiesDeck() {
        Deck deck = Deck.createDeck();
        deck.draw(24);
        assertEquals(0, deck.view().size());
    }

    @Test
    void drawMoreThanAvailableThrows() {
        Deck deck = Deck.createDeck();
        deck.draw(24);
        assertThrows(Exception.class, () -> deck.draw(1));
    }

    @Test
    void viewReturnsUnmodifiableList() {
        Deck deck = Deck.createDeck();
        List<Card> view = deck.view();
        assertThrows(UnsupportedOperationException.class,
                () -> view.remove(0));
    }
}
