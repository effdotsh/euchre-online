import org.example.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayerTest {

    @Test
    void aiChoosesExpectedRandomCardWithSeededRandom() {
        AIPlayer aiPlayer = new AIPlayer("AI", new Random(1234));
        aiPlayer.addCard(new Card(Suit.HEARTS, Rank.NINE));
        aiPlayer.addCard(new Card(Suit.SPADES, Rank.ACE));
        aiPlayer.addCard(new Card(Suit.CLUBS, Rank.TEN));

        Card selected = aiPlayer.chooseCard(null, Suit.HEARTS);

        assertEquals(Suit.CLUBS, selected.getSuit());
        assertEquals(Rank.TEN, selected.getRank());
    }

    @Test
    void aiThrowsWhenNoLegalCards() {
        AIPlayer aiPlayer = new AIPlayer("AI");

        assertThrows(IllegalStateException.class, () -> aiPlayer.chooseCard(null, Suit.HEARTS));
    }

    @Test
    void playerHandManagementWorks() {
        Player player = new HumanPlayer("Human");
        Card card = new Card(Suit.DIAMONDS, Rank.QUEEN);

        player.addCard(card);
        assertEquals(1, player.getHand().size());

        player.removeCard(card);
        assertEquals(0, player.getHand().size());
    }

    @Test
    void handViewIsUnmodifiable() {
        Player player = new HumanPlayer("Human");
        player.addCard(new Card(Suit.HEARTS, Rank.ACE));

        List<Card> hand = player.getHand();
        assertThrows(UnsupportedOperationException.class,
                () -> hand.add(new Card(Suit.SPADES, Rank.NINE)));
    }

    @Test
    void humanPlaceholderSelectsFirstLegalCard() {
        HumanPlayer human = new HumanPlayer("Human");
        human.addCard(new Card(Suit.HEARTS, Rank.KING));
        human.addCard(new Card(Suit.SPADES, Rank.NINE));

        Card selected = human.chooseCard(null, Suit.CLUBS);

        assertEquals(Suit.HEARTS, selected.getSuit());
        assertEquals(Rank.KING, selected.getRank());
    }

    @Test
    void legalPlayableCardsWhenLeadingReturnsWholeHand() {
        Player player = new HumanPlayer("Human");
        Card c1 = new Card(Suit.HEARTS, Rank.ACE);
        Card c2 = new Card(Suit.CLUBS, Rank.NINE);
        player.addCard(c1);
        player.addCard(c2);

        List<Card> legal = player.getLegalCards(Suit.SPADES, null);

        assertEquals(2, legal.size());
        assertEquals(c1, legal.get(0));
        assertEquals(c2, legal.get(1));
    }

    @Test
    void legalPlayableCardsMustFollowLedSuitWhenPossible() {
        Player player = new HumanPlayer("Human");
        Card hearts = new Card(Suit.HEARTS, Rank.KING);
        Card clubs = new Card(Suit.CLUBS, Rank.NINE);
        player.addCard(hearts);
        player.addCard(clubs);

        List<Card> legal = player.getLegalCards(Suit.SPADES, Suit.HEARTS);

        assertEquals(1, legal.size());
        assertEquals(hearts, legal.getFirst());
    }

    @Test
    void legalPlayableCardsReturnsWholeHandWhenCannotFollowSuit() {
        Player player = new HumanPlayer("Human");
        Card clubs = new Card(Suit.CLUBS, Rank.NINE);
        Card spades = new Card(Suit.SPADES, Rank.TEN);
        player.addCard(clubs);
        player.addCard(spades);

        List<Card> legal = player.getLegalCards(Suit.HEARTS, Suit.DIAMONDS);

        assertEquals(2, legal.size());
        assertEquals(clubs, legal.get(0));
        assertEquals(spades, legal.get(1));
    }

    @Test
    void legalPlayableCardsTreatsLeftBowerAsTrump() {
        Player player = new HumanPlayer("Human");
        Card leftBower = new Card(Suit.DIAMONDS, Rank.JACK); // left bower when hearts is trump
        Card offSuit = new Card(Suit.CLUBS, Rank.NINE);
        player.addCard(leftBower);
        player.addCard(offSuit);

        List<Card> legal = player.getLegalCards(Suit.HEARTS, Suit.HEARTS);

        assertEquals(1, legal.size());
        assertEquals(leftBower, legal.getFirst());
    }
}
