import org.example.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void aiChoosesExpectedRandomCardWithSeededRandom() {
        RandomAIPlayer randomAiPlayer = new RandomAIPlayer("AI", new Random(1234));
        randomAiPlayer.addCard(new Card(Suit.HEARTS, Rank.NINE));
        randomAiPlayer.addCard(new Card(Suit.SPADES, Rank.ACE));
        randomAiPlayer.addCard(new Card(Suit.CLUBS, Rank.TEN));

        Card selected = randomAiPlayer.playCard(null, Suit.HEARTS);

        assertEquals(Suit.HEARTS, selected.getSuit());
        assertEquals(Rank.NINE, selected.getRank());
    }

    @Test
    void aiThrowsWhenNoLegalCards() {
        RandomAIPlayer randomAiPlayer = new RandomAIPlayer("AI");

        assertThrows(IllegalStateException.class, () -> randomAiPlayer.playCard(null, Suit.HEARTS));
    }

    @Test
    void playerHandManagementWorks() {
        Player player = new CLIPlayer("Human");
        Card card = new Card(Suit.DIAMONDS, Rank.QUEEN);

        player.addCard(card);
        assertEquals(1, player.getHand().size());

        player.removeCard(card);
        assertEquals(0, player.getHand().size());
    }

    @Test
    void handViewIsUnmodifiable() {
        Player player = new CLIPlayer("Human");
        player.addCard(new Card(Suit.HEARTS, Rank.ACE));

        List<Card> hand = player.getHand();
        assertThrows(UnsupportedOperationException.class,
                () -> hand.add(new Card(Suit.SPADES, Rank.NINE)));
    }

    @Test
    void humanPlaceholderSelectsFirstLegalCard() {
        CLIPlayer human = new CLIPlayer("Human");
        human.addCard(new Card(Suit.HEARTS, Rank.KING));
        human.addCard(new Card(Suit.SPADES, Rank.NINE));

        Card selected = human.playCard(null, Suit.CLUBS);

        assertEquals(Suit.HEARTS, selected.getSuit());
        assertEquals(Rank.KING, selected.getRank());
    }

    @Test
    void legalPlayableCardsWhenLeadingReturnsWholeHand() {
        Player player = new CLIPlayer("Human");
        Card card1 = new Card(Suit.HEARTS, Rank.ACE);
        Card card2 = new Card(Suit.CLUBS, Rank.NINE);
        player.addCard(card1);
        player.addCard(card2);

        List<Card> legal = player.getLegalCards(Suit.SPADES, null);

        assertEquals(2, legal.size());
        assertEquals(card1, legal.get(0));
        assertEquals(card2, legal.get(1));
    }

    @Test
    void legalPlayableCardsMustFollowLedSuitWhenPossible() {
        Player player = new CLIPlayer("Human");
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
        Player player = new CLIPlayer("Human");
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
        Player player = new CLIPlayer("Human");
        Card leftBower = new Card(Suit.DIAMONDS, Rank.JACK); // left bower when hearts is trump
        Card offSuit = new Card(Suit.CLUBS, Rank.NINE);
        player.addCard(leftBower);
        player.addCard(offSuit);

        List<Card> legal = player.getLegalCards(Suit.HEARTS, Suit.HEARTS);

        assertEquals(1, legal.size());
        assertEquals(leftBower, legal.getFirst());
    }

    @Test
    void playCardRemovesCardFromHand() {
        Player player = new RandomAIPlayer("Test", new Random(42));
        player.setHand(new java.util.ArrayList<>(List.of(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.CLUBS, Rank.NINE)
        )));

        Card played = player.playCard(Suit.HEARTS, null);
        assertNotNull(played);
        assertEquals(2, player.getHand().size());
        assertFalse(player.getHand().contains(played));
    }

    @Test
    void setHandReplacesExistingHand() {
        Player player = new RandomAIPlayer("Test");
        player.addCard(new Card(Suit.HEARTS, Rank.ACE));
        assertEquals(1, player.getHand().size());

        player.setHand(new java.util.ArrayList<>(List.of(
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.SPADES, Rank.KING)
        )));
        assertEquals(2, player.getHand().size());
    }
}
