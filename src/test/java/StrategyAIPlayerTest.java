import org.example.*;
import org.example.Players.Player;
import org.example.Players.StrategyAIPlayer;
import org.example.Strategies.StrategyFactory;
import org.example.Strategies.StrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StrategyAIPlayerTest {

    private StrategyAIPlayer player;

    @BeforeEach
    void setUp() {
        player = new StrategyAIPlayer("TestPlayer", StrategyFactory.create(StrategyType.NEUTRAL));
    }

    @Test
    void leadCardUsesHighestNonTrumpWhenNoTrumpInHand() {
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.DIAMONDS, Rank.QUEEN),
                new Card(Suit.CLUBS, Rank.KING)
        )));

        Card played = player.playCard(Suit.HEARTS, Optional.empty(), List.of());

        assertEquals(new Card(Suit.CLUBS, Rank.KING), played);
    }

    @Test
    void fallbackUsesHighestNonTrumpAceWhenAvailable() {
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.CLUBS, Rank.KING),
                new Card(Suit.DIAMONDS, Rank.NINE)
        )));

        Card played = player.playCard(Suit.HEARTS, Optional.of(Suit.HEARTS), List.of());

        assertEquals(new Card(Suit.SPADES, Rank.ACE), played);
    }

    @Test
    void fallbackUsesHighestNonTrumpPlayWhenNoAceExists() {
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.DIAMONDS, Rank.QUEEN),
                new Card(Suit.CLUBS, Rank.NINE)
        )));

        Card played = player.playCard(Suit.HEARTS, Optional.of(Suit.HEARTS), List.of());

        assertEquals(new Card(Suit.CLUBS, Rank.NINE), played);
    }

    @Test
    void chooseToOrderUpDelegatesToOpponentOverload() {
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.SPADES, Rank.NINE),
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.TEN),
                new Card(Suit.HEARTS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.KING)
        )));

        Card upCard = new Card(Suit.HEARTS, Rank.NINE);
        Bid viaSimple = player.chooseToOrderUp(upCard);
        Bid viaExplicit = player.chooseToOrderUp(upCard, UpcardRecipient.OPPONENT);

        assertEquals(viaExplicit.getType(), viaSimple.getType());
        assertEquals(viaExplicit.getTrump(), viaSimple.getTrump());
        assertEquals(viaExplicit.isAlone(), viaSimple.isAlone());
    }

    @Test
    void partnerWinningWithNonTrumpAceUsesSafeDiscardPath() {
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.HEARTS, Rank.NINE),
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.SPADES, Rank.TEN)
        )));

        Card played = player.playCard(
                Suit.SPADES,
                Optional.of(Suit.HEARTS),
                List.of(new Player.PlayedCard(new Card(Suit.HEARTS, Rank.ACE), Player.PlayedBy.PARTNER, 1))
        );

        assertEquals(new Card(Suit.HEARTS, Rank.NINE), played);
    }

}



