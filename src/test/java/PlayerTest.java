import org.example.Card;
import org.example.Players.Player;
import org.example.Players.RandomAIPlayer;
import org.example.Players.StrategyAIPlayer;
import org.example.Rank;
import org.example.Suit;
import org.example.UpcardRecipient;
import org.example.strategies.StrategyFactory;
import org.example.strategies.StrategyType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private StrategyAIPlayer neutralStrategyPlayer() {
        return new StrategyAIPlayer("AI", StrategyFactory.create(StrategyType.NEUTRAL));
    }

    private void setAggressiveOnlyOrderUpHand(StrategyAIPlayer player) {
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.HEARTS, Rank.KING),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.DIAMONDS, Rank.NINE)
        )));
    }

    private void setNeutralNotConservativeOrderUpHand(StrategyAIPlayer player) {
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.DIAMONDS, Rank.JACK),
                new Card(Suit.HEARTS, Rank.TEN),
                new Card(Suit.CLUBS, Rank.QUEEN),
                new Card(Suit.CLUBS, Rank.JACK),
                new Card(Suit.SPADES, Rank.KING)
        )));
    }

    @Test
    void aiChoosesExpectedRandomCardWithSeededRandom() {
        RandomAIPlayer randomAiPlayer = new RandomAIPlayer("AI", new Random(1234));
        randomAiPlayer.addCard(new Card(Suit.HEARTS, Rank.NINE));
        randomAiPlayer.addCard(new Card(Suit.SPADES, Rank.ACE));
        randomAiPlayer.addCard(new Card(Suit.CLUBS, Rank.TEN));

        Card selected = randomAiPlayer.playCard(null, Optional.of(Suit.HEARTS), List.of());

        assertEquals(Suit.HEARTS, selected.getSuit());
        assertEquals(Rank.NINE, selected.getRank());
    }

    @Test
    void aiThrowsWhenNoLegalCards() {
        RandomAIPlayer randomAiPlayer = new RandomAIPlayer("AI");

        assertThrows(IllegalStateException.class, () -> randomAiPlayer.playCard(null, Optional.of(Suit.HEARTS), List.of()));
    }

    @Test
    void playerHandManagementWorks() {
        Player player = new RandomAIPlayer("Robot");
        Card card = new Card(Suit.DIAMONDS, Rank.QUEEN);

        player.addCard(card);
        assertEquals(1, player.getHand().size());

        player.removeCard(card);
        assertEquals(0, player.getHand().size());
    }

    @Test
    void handViewIsUnmodifiable() {
        Player player = new RandomAIPlayer("Robot");
        player.addCard(new Card(Suit.HEARTS, Rank.ACE));

        List<Card> hand = player.getHand();
        assertThrows(UnsupportedOperationException.class,
                () -> hand.add(new Card(Suit.SPADES, Rank.NINE)));
    }

    @Test
    void legalPlayableCardsWhenLeadingReturnsWholeHand() {
        Player player = new RandomAIPlayer("Robot");
        Card card1 = new Card(Suit.HEARTS, Rank.ACE);
        Card card2 = new Card(Suit.CLUBS, Rank.NINE);
        player.addCard(card1);
        player.addCard(card2);

        List<Card> legal = player.getLegalCards(Suit.SPADES, Optional.empty());

        assertEquals(2, legal.size());
        assertEquals(card1, legal.get(0));
        assertEquals(card2, legal.get(1));
    }

    @Test
    void legalPlayableCardsMustFollowLedSuitWhenPossible() {
        Player player = new RandomAIPlayer("Robot");
        Card hearts = new Card(Suit.HEARTS, Rank.KING);
        Card clubs = new Card(Suit.CLUBS, Rank.NINE);
        player.addCard(hearts);
        player.addCard(clubs);

        List<Card> legal = player.getLegalCards(Suit.SPADES, Optional.of(Suit.HEARTS));

        assertEquals(1, legal.size());
        assertEquals(hearts, legal.getFirst());
    }

    @Test
    void legalPlayableCardsReturnsWholeHandWhenCannotFollowSuit() {
        Player player = new RandomAIPlayer("Robot");
        Card clubs = new Card(Suit.CLUBS, Rank.NINE);
        Card spades = new Card(Suit.SPADES, Rank.TEN);
        player.addCard(clubs);
        player.addCard(spades);

        List<Card> legal = player.getLegalCards(Suit.HEARTS, Optional.of(Suit.DIAMONDS));

        assertEquals(2, legal.size());
        assertEquals(clubs, legal.get(0));
        assertEquals(spades, legal.get(1));
    }

    @Test
    void legalPlayableCardsTreatsLeftBowerAsTrump() {
        Player player = new RandomAIPlayer("Robot");
        Card leftBower = new Card(Suit.DIAMONDS, Rank.JACK); // left bower when hearts is trump
        Card offSuit = new Card(Suit.CLUBS, Rank.NINE);
        player.addCard(leftBower);
        player.addCard(offSuit);

        List<Card> legal = player.getLegalCards(Suit.HEARTS, Optional.of(Suit.HEARTS));

        assertEquals(1, legal.size());
        assertEquals(leftBower, legal.getFirst());
    }

    @Test
    void playCardRemovesCardFromHand() {
        Player player = new RandomAIPlayer("Test", new Random(42));
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.HEARTS, Rank.ACE),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.CLUBS, Rank.NINE)
        )));

        Card played = player.playCard(Suit.HEARTS, Optional.empty(), List.of());
        assertNotNull(played);
        assertEquals(2, player.getHand().size());
        assertFalse(player.getHand().contains(played));
    }

    @Test
    void strategyPlayerLeadsRightBowerWhenItHasTrumpAndNoLeadExists() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card rightBower = new Card(Suit.SPADES, Rank.JACK);
        Card ace = new Card(Suit.HEARTS, Rank.ACE);
        Card club = new Card(Suit.CLUBS, Rank.NINE);
        player.setHand(new ArrayList<>(List.of(rightBower, ace, club)));

        Card played = player.playCard(Suit.SPADES, Optional.empty(), List.of());

        assertEquals(rightBower.getId(), played.getId());
    }

    @Test
    void strategyPlayerFollowsNonTrumpSuitWithHighestCardUnlessPartnerPlayedAce() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card nine = new Card(Suit.HEARTS, Rank.NINE);
        Card king = new Card(Suit.HEARTS, Rank.KING);
        Card club = new Card(Suit.CLUBS, Rank.ACE);
        player.setHand(new ArrayList<>(List.of(nine, king, club)));

        List<Player.PlayedCard> playedCards = List.of(
                new Player.PlayedCard(new Card(Suit.HEARTS, Rank.TEN), Player.PlayedBy.OPPONENT, 1)
        );

        Card played = player.playCard(Suit.SPADES, Optional.of(Suit.HEARTS), playedCards);

        assertEquals(king.getId(), played.getId());
    }

    @Test
    void strategyPlayerPlaysLowestWhenPartnerAlreadyPlayedAceInLedSuit() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card nine = new Card(Suit.HEARTS, Rank.NINE);
        Card king = new Card(Suit.HEARTS, Rank.KING);
        Card club = new Card(Suit.CLUBS, Rank.ACE);
        player.setHand(new ArrayList<>(List.of(nine, king, club)));

        List<Player.PlayedCard> playedCards = List.of(
                new Player.PlayedCard(new Card(Suit.HEARTS, Rank.ACE), Player.PlayedBy.PARTNER, 1)
        );

        Card played = player.playCard(Suit.SPADES, Optional.of(Suit.HEARTS), playedCards);

        assertEquals(nine.getId(), played.getId());
    }

    @Test
    void strategyPlayerUsesLowestTrumpToBeatOpponentWhenItCanTakeTheTrick() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card lowTrump = new Card(Suit.SPADES, Rank.NINE);
        Card highTrump = new Card(Suit.SPADES, Rank.KING);
        Card offSuit = new Card(Suit.CLUBS, Rank.QUEEN);
        player.setHand(new ArrayList<>(List.of(lowTrump, highTrump, offSuit)));

        List<Player.PlayedCard> playedCards = List.of(
                new Player.PlayedCard(new Card(Suit.HEARTS, Rank.QUEEN), Player.PlayedBy.OPPONENT, 1)
        );

        Card played = player.playCard(Suit.SPADES, Optional.of(Suit.HEARTS), playedCards);

        assertEquals(lowTrump.getId(), played.getId());
    }

    @Test
    void strategyPlayerShortSuitsWhenPartnerIsWinningOnTrump() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card safeDiscard = new Card(Suit.CLUBS, Rank.NINE);
        Card otherDiscard = new Card(Suit.DIAMONDS, Rank.TEN);
        Card trump = new Card(Suit.SPADES, Rank.NINE);
        player.setHand(new ArrayList<>(List.of(safeDiscard, otherDiscard, trump)));

        List<Player.PlayedCard> playedCards = List.of(
                new Player.PlayedCard(new Card(Suit.SPADES, Rank.KING), Player.PlayedBy.PARTNER, 1)
        );

        Card played = player.playCard(Suit.SPADES, Optional.of(Suit.HEARTS), playedCards);

        assertEquals(safeDiscard.getId(), played.getId());
    }

    @Test
    void strategyPlayerLeadsShortSuitLowCardWhenBackedByTrump() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card highestNonTrump = new Card(Suit.HEARTS, Rank.KING);
        Card lowerNonTrump = new Card(Suit.HEARTS, Rank.NINE);
        Card trumpCard = new Card(Suit.SPADES, Rank.NINE);
        player.setHand(new ArrayList<>(List.of(lowerNonTrump, trumpCard, highestNonTrump)));

        Card played = player.playCard(Suit.SPADES, Optional.empty(), List.of());

        assertEquals(lowerNonTrump.getId(), played.getId());
    }

    @Test
    void strategyPlayerFallsBackToShortSuitSafeDiscardBeforeBurningAce() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card ace = new Card(Suit.HEARTS, Rank.ACE);
        Card king = new Card(Suit.SPADES, Rank.KING);
        Card trumpCard = new Card(Suit.DIAMONDS, Rank.NINE);
        player.setHand(new ArrayList<>(List.of(king, trumpCard, ace)));

        Card played = player.playCard(Suit.DIAMONDS, Optional.of(Suit.CLUBS), List.of());

        assertEquals(king.getId(), played.getId());
    }

    @Test
    void strategyPlayerFallsBackToLowestSafeShortSuitCardWhenNoAceExists() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card highestNonTrump = new Card(Suit.HEARTS, Rank.KING);
        Card lowerNonTrump = new Card(Suit.CLUBS, Rank.QUEEN);
        Card trumpCard = new Card(Suit.DIAMONDS, Rank.NINE);
        player.setHand(new ArrayList<>(List.of(lowerNonTrump, trumpCard, highestNonTrump)));

        Card played = player.playCard(Suit.DIAMONDS, Optional.of(Suit.SPADES), List.of());

        assertEquals(lowerNonTrump.getId(), played.getId());
    }

    @Test
    void strategyPlayerUsesLowestWinningTrumpAgainstOpponentTrumpLead() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card lowWinningTrump = new Card(Suit.SPADES, Rank.KING);
        Card highWinningTrump = new Card(Suit.SPADES, Rank.JACK);
        Card offSuit = new Card(Suit.CLUBS, Rank.NINE);
        player.setHand(new ArrayList<>(List.of(offSuit, highWinningTrump, lowWinningTrump)));

        List<Player.PlayedCard> playedCards = List.of(
                new Player.PlayedCard(new Card(Suit.SPADES, Rank.QUEEN), Player.PlayedBy.OPPONENT, 1)
        );

        Card played = player.playCard(Suit.SPADES, Optional.of(Suit.HEARTS), playedCards);

        assertEquals(lowWinningTrump.getId(), played.getId());
    }

    @Test
    void strategyPlayerUsesFallbackWhenPartnerWinningButNoSafeDiscardExists() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card offSuitAce = new Card(Suit.HEARTS, Rank.ACE);
        Card trumpCard = new Card(Suit.SPADES, Rank.NINE);
        player.setHand(new ArrayList<>(List.of(trumpCard, offSuitAce)));

        List<Player.PlayedCard> playedCards = List.of(
                new Player.PlayedCard(new Card(Suit.SPADES, Rank.KING), Player.PlayedBy.PARTNER, 1)
        );

        Card played = player.playCard(Suit.SPADES, Optional.of(Suit.CLUBS), playedCards);

        assertEquals(offSuitAce.getId(), played.getId());
    }

    @Test
    void strategyPlayerChooseDiscardPrefersLowestSafeNonTrump() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card rightBower = new Card(Suit.SPADES, Rank.JACK);
        Card offSuitAce = new Card(Suit.HEARTS, Rank.ACE);
        Card lowestSafeDiscard = new Card(Suit.CLUBS, Rank.NINE);
        Card otherSafeDiscard = new Card(Suit.DIAMONDS, Rank.TEN);
        player.setHand(new ArrayList<>(List.of(rightBower, offSuitAce, otherSafeDiscard, lowestSafeDiscard)));

        Card discarded = player.chooseDiscard(Suit.SPADES, Optional.empty(), List.of());

        assertEquals(lowestSafeDiscard.getId(), discarded.getId());
    }

    @Test
    void strategyPlayerChooseDiscardFallsBackToLowestCardWhenNoSafeDiscardExists() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card lowTrump = new Card(Suit.SPADES, Rank.NINE);
        Card highTrump = new Card(Suit.SPADES, Rank.KING);
        player.setHand(new ArrayList<>(List.of(highTrump, lowTrump)));

        Card discarded = player.chooseDiscard(Suit.SPADES, Optional.empty(), List.of());

        assertEquals(lowTrump.getId(), discarded.getId());
    }

    @Test
    void strategyPlayerChooseDiscardPrefersShortSuitWhenBackedByTrump() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card trumpCard = new Card(Suit.SPADES, Rank.KING);
        Card shortSuitCard = new Card(Suit.CLUBS, Rank.QUEEN);
        Card longSuitLow = new Card(Suit.HEARTS, Rank.NINE);
        Card longSuitHigh = new Card(Suit.HEARTS, Rank.KING);
        player.setHand(new ArrayList<>(List.of(trumpCard, shortSuitCard, longSuitLow, longSuitHigh)));

        Card discarded = player.chooseDiscard(Suit.SPADES, Optional.empty(), List.of());

        assertEquals(shortSuitCard.getId(), discarded.getId());
    }

    @Test
    void strategyPlayerChooseDiscardNeverBurnsAceWhenShortSuitOptionExists() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card trumpCard = new Card(Suit.SPADES, Rank.NINE);
        Card shortSuitCard = new Card(Suit.CLUBS, Rank.TEN);
        Card offSuitAce = new Card(Suit.DIAMONDS, Rank.ACE);
        player.setHand(new ArrayList<>(List.of(trumpCard, offSuitAce, shortSuitCard)));

        Card discarded = player.chooseDiscard(Suit.SPADES, Optional.empty(), List.of());

        assertEquals(shortSuitCard.getId(), discarded.getId());
    }

    @Test
    void strategyPlayerLosingTrickPlaysLowestNonTrumpFirst() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card lowNonTrump = new Card(Suit.CLUBS, Rank.NINE);
        Card highNonTrump = new Card(Suit.HEARTS, Rank.KING);
        Card trumpCard = new Card(Suit.SPADES, Rank.NINE);
        player.setHand(new ArrayList<>(List.of(highNonTrump, trumpCard, lowNonTrump)));

        List<Player.PlayedCard> playedCards = List.of(
                new Player.PlayedCard(new Card(Suit.SPADES, Rank.JACK), Player.PlayedBy.OPPONENT, 1)
        );

        Card played = player.playCard(Suit.SPADES, Optional.of(Suit.DIAMONDS), playedCards);

        assertEquals(lowNonTrump.getId(), played.getId());
    }

    @Test
    void strategyPlayerLosingTrickWithOnlyTrumpPlaysLowestTrump() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        Card lowTrump = new Card(Suit.SPADES, Rank.NINE);
        Card highTrump = new Card(Suit.SPADES, Rank.KING);
        player.setHand(new ArrayList<>(List.of(highTrump, lowTrump)));

        List<Player.PlayedCard> playedCards = List.of(
                new Player.PlayedCard(new Card(Suit.SPADES, Rank.JACK), Player.PlayedBy.OPPONENT, 1)
        );

        Card played = player.playCard(Suit.SPADES, Optional.of(Suit.HEARTS), playedCards);

        assertEquals(lowTrump.getId(), played.getId());
    }

    @Test
    void strategyPlayerBecomesAggressiveWhenTrailingByMoreThanThree() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        setAggressiveOnlyOrderUpHand(player);

        assertFalse(player.chooseToOrderUp(new Card(Suit.HEARTS, Rank.JACK), UpcardRecipient.PARTNER));

        player.updateScoreContext(2, 6);

        assertTrue(player.chooseToOrderUp(new Card(Suit.HEARTS, Rank.JACK), UpcardRecipient.PARTNER));
    }

    @Test
    void strategyPlayerBecomesConservativeWhenLeadingByMoreThanThree() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        setNeutralNotConservativeOrderUpHand(player);

        assertTrue(player.chooseToOrderUp(new Card(Suit.HEARTS, Rank.JACK), UpcardRecipient.PARTNER));

        player.updateScoreContext(8, 4);

        assertFalse(player.chooseToOrderUp(new Card(Suit.HEARTS, Rank.JACK), UpcardRecipient.PARTNER));
    }

    @Test
    void strategyPlayerReturnsToNeutralWhenScoreDiffMovesBackInsideThresholds() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        setAggressiveOnlyOrderUpHand(player);

        player.updateScoreContext(1, 6);
        assertTrue(player.chooseToOrderUp(new Card(Suit.HEARTS, Rank.JACK), UpcardRecipient.PARTNER));

        player.updateScoreContext(5, 5);
        assertFalse(player.chooseToOrderUp(new Card(Suit.HEARTS, Rank.JACK), UpcardRecipient.PARTNER));
    }

    @Test
    void strategyPlayerDoesNotBecomeAggressiveAtNegativeThreeBoundary() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        setAggressiveOnlyOrderUpHand(player);

        player.updateScoreContext(2, 5);

        assertFalse(player.chooseToOrderUp(new Card(Suit.HEARTS, Rank.JACK), UpcardRecipient.PARTNER));
    }

    @Test
    void strategyPlayerDoesNotBecomeConservativeAtPositiveThreeBoundary() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        setNeutralNotConservativeOrderUpHand(player);

        player.updateScoreContext(6, 3);

        assertTrue(player.chooseToOrderUp(new Card(Suit.HEARTS, Rank.JACK), UpcardRecipient.PARTNER));
    }

    @Test
    void setHandReplacesExistingHand() {
        Player player = new RandomAIPlayer("Test");
        player.addCard(new Card(Suit.HEARTS, Rank.ACE));
        assertEquals(1, player.getHand().size());

        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.SPADES, Rank.KING)
        )));
        assertEquals(2, player.getHand().size());
    }

    @Test
    void strategyPlayerCallsHighestSuitWhenForcedToCallTrumpWithPoorHand() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.HEARTS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.SPADES, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.TEN),
                new Card(Suit.CLUBS, Rank.QUEEN)
        )));


        Optional<Suit> called = player.chooseToCallTrump(Suit.HEARTS, true);

        assertTrue(called.isPresent(), "Player should be forced to call a suit");
        assertNotEquals(Suit.HEARTS, called.get(), "Should not call the forbidden suit");
    }

    @Test
    void strategyPlayerCallsBestNonForbiddenSuitWhenStuck() {
        StrategyAIPlayer player = neutralStrategyPlayer();
        player.setHand(new ArrayList<>(List.of(
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.HEARTS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.DIAMONDS, Rank.TEN)
        )));

        Optional<Suit> called = player.chooseToCallTrump(Suit.HEARTS, true);

        assertTrue(called.isPresent(), "Player should be forced to call a suit");
        assertEquals(Suit.SPADES, called.get(), "Should call the best suit (spades with jack and ace)");
    }
}
