import org.example.Card;
import org.example.Rank;
import org.example.Suit;
import org.example.UpcardRecipient;
import org.example.strategies.AggressiveStrategy;
import org.example.strategies.ConservativeStrategy;
import org.example.strategies.EuchreAIStrategy;
import org.example.strategies.NeutralStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategiesTest {

    @Test
    void aggressiveChooseCallTrumpReturnsBestSuitWhenThresholdIsMet() {
        EuchreAIStrategy conservativeStrategy = new ConservativeStrategy();
        EuchreAIStrategy aggressive = new AggressiveStrategy();
        EuchreAIStrategy neutral = new NeutralStrategy();
        List<Card> hand = cards(
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.CLUBS, Rank.JACK),
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.HEARTS, Rank.NINE)
        );

        assertEquals(Optional.of(Suit.SPADES), conservativeStrategy.chooseCallTrump(Suit.HEARTS, hand));
        assertEquals(Optional.of(Suit.SPADES), neutral.chooseCallTrump(Suit.HEARTS, hand));
        assertEquals(Optional.of(Suit.SPADES), aggressive.chooseCallTrump(Suit.HEARTS, hand));
    }

    @Test
    void aggressiveChooseCallTrumpRespectsForbiddenSuitWhenSelectingBestSuit() {
        AggressiveStrategy aggressive = new AggressiveStrategy();
        List<Card> hand = cards(
                new Card(Suit.SPADES, Rank.JACK),
                new Card(Suit.CLUBS, Rank.JACK),
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.SPADES, Rank.KING),
                new Card(Suit.HEARTS, Rank.NINE)
        );

        assertEquals(Optional.of(Suit.CLUBS), aggressive.chooseCallTrump(Suit.SPADES, hand));
    }

    @Test
    void aggressiveChooseCallTrumpReturnsEmptyWhenNoSuitMeetsThreshold() {
        AggressiveStrategy aggressive = new AggressiveStrategy();
        List<Card> hand = cards(
                new Card(Suit.HEARTS, Rank.NINE),
                new Card(Suit.CLUBS, Rank.NINE),
                new Card(Suit.SPADES, Rank.TEN),
                new Card(Suit.DIAMONDS, Rank.TEN),
                new Card(Suit.CLUBS, Rank.QUEEN)
        );

        assertTrue(aggressive.chooseCallTrump(Suit.HEARTS, hand).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("orderUpScenarios")
    void shouldOrderUpMatchTheExpectedStrategyCalls(OrderUpScenario scenario) {
        assertStrategyDecision("aggressive", new AggressiveStrategy(), scenario, scenario.aggressiveExpected());
        assertStrategyDecision("neutral", new NeutralStrategy(), scenario, scenario.neutralExpected());
        assertStrategyDecision("conservative", new ConservativeStrategy(), scenario, scenario.conservativeExpected());
    }

    private static Stream<OrderUpScenario> orderUpScenarios() {
        return Stream.of(
                OrderUpScenario.of(
                        "1. J♥ to partner; J♦, K♣, Q♣, 9♦, 10♠ — none",
                        cards(
                                new Card(Suit.DIAMONDS, Rank.JACK),
                                new Card(Suit.CLUBS, Rank.KING),
                                new Card(Suit.CLUBS, Rank.QUEEN),
                                new Card(Suit.DIAMONDS, Rank.NINE),
                                new Card(Suit.SPADES, Rank.TEN)
                        ),
                        false,
                        false,
                        false
                ),
                OrderUpScenario.of(
                        "2. J♥ to partner; A♥, K♥, Q♣, J♠, 9♦ — aggressive calls",
                        cards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.HEARTS, Rank.KING),
                                new Card(Suit.CLUBS, Rank.QUEEN),
                                new Card(Suit.SPADES, Rank.JACK),
                                new Card(Suit.DIAMONDS, Rank.NINE)
                        ),
                        true,
                        false,
                        false
                ),
                OrderUpScenario.of(
                        "3. J♥ to partner; 10♥, 9♥, K♣, Q♠, Q♦ — nobody calls",
                        cards(
                                new Card(Suit.HEARTS, Rank.TEN),
                                new Card(Suit.HEARTS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.KING),
                                new Card(Suit.SPADES, Rank.QUEEN),
                                new Card(Suit.DIAMONDS, Rank.QUEEN)
                        ),
                        false,
                        false,
                        false
                ),
                OrderUpScenario.of(
                        "4. J♥ to partner; A♥, 10♥, K♣, Q♠, Q♦ — aggressive only",
                        cards(
                                new Card(Suit.HEARTS, Rank.ACE),
                                new Card(Suit.HEARTS, Rank.TEN),
                                new Card(Suit.CLUBS, Rank.KING),
                                new Card(Suit.SPADES, Rank.QUEEN),
                                new Card(Suit.DIAMONDS, Rank.QUEEN)
                        ),
                        true,
                        false,
                        false
                ),
                OrderUpScenario.of(
                        "5. J♥ to partner; J♦, 10♥, Q♣, J♣, K♠ — aggressive and neutral",
                        cards(
                                new Card(Suit.DIAMONDS, Rank.JACK),
                                new Card(Suit.HEARTS, Rank.TEN),
                                new Card(Suit.CLUBS, Rank.QUEEN),
                                new Card(Suit.CLUBS, Rank.JACK),
                                new Card(Suit.SPADES, Rank.KING)
                        ),
                        true,
                        true,
                        false
                ),
                OrderUpScenario.of(
                        "6. J♥ to partner; J♦, 10♥, A♠, K♠, Q♦ — all three call",
                        cards(
                                new Card(Suit.DIAMONDS, Rank.JACK),
                                new Card(Suit.HEARTS, Rank.TEN),
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.SPADES, Rank.KING),
                                new Card(Suit.DIAMONDS, Rank.QUEEN)
                        ),
                        true,
                        true,
                        true
                ),
                OrderUpScenario.of(
                        "7. J♥ to partner; Q♥, A♠, 10♠, A♦, Q♦ — aggressive and neutral",
                        cards(
                                new Card(Suit.HEARTS, Rank.QUEEN),
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.SPADES, Rank.TEN),
                                new Card(Suit.DIAMONDS, Rank.ACE),
                                new Card(Suit.DIAMONDS, Rank.QUEEN)
                        ),
                        true,
                        true,
                        false
                ),
                OrderUpScenario.of(
                        "8. J♥ to partner; Q♥, 10♥, 9♥, K♣, Q♣ — all three call",
                        cards(
                                new Card(Suit.HEARTS, Rank.QUEEN),
                                new Card(Suit.HEARTS, Rank.TEN),
                                new Card(Suit.HEARTS, Rank.NINE),
                                new Card(Suit.CLUBS, Rank.KING),
                                new Card(Suit.CLUBS, Rank.QUEEN)
                        ),
                        true,
                        true,
                        true
                ),
                OrderUpScenario.of(
                        "9. J♥ to partner; A♣, A♠, A♦, 10♦, 9♦ — aggressive and neutral",
                        cards(
                                new Card(Suit.CLUBS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.DIAMONDS, Rank.ACE),
                                new Card(Suit.DIAMONDS, Rank.TEN),
                                new Card(Suit.DIAMONDS, Rank.NINE)
                        ),
                        true,
                        true,
                        false
                ),
                OrderUpScenario.of(
                        "10. J♥ to partner; 10♥, 9♥, K♠, Q♠, Q♣ — aggressive only",
                        cards(
                                new Card(Suit.HEARTS, Rank.TEN),
                                new Card(Suit.HEARTS, Rank.NINE),
                                new Card(Suit.SPADES, Rank.KING),
                                new Card(Suit.SPADES, Rank.QUEEN),
                                new Card(Suit.CLUBS, Rank.QUEEN)
                        ),
                        true,
                        false,
                        false
                ),
                OrderUpScenario.of(
                        "11. K♠ to opponents; J♣, J♠, A♠, 9♣, 9♦ — all three call",
                        new Card(Suit.SPADES, Rank.KING),
                        cards(
                                new Card(Suit.CLUBS, Rank.JACK),
                                new Card(Suit.SPADES, Rank.JACK),
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.CLUBS, Rank.NINE),
                                new Card(Suit.DIAMONDS, Rank.NINE)
                        ),
                        UpcardRecipient.OPPONENT,
                        true,
                        true,
                        true
                ),
                OrderUpScenario.of(
                        "12. 9♠ to partner; J♣, J♠, A♠, 9♣, 9♦ — all three call",
                        new Card(Suit.SPADES, Rank.NINE),
                        cards(
                                new Card(Suit.CLUBS, Rank.JACK),
                                new Card(Suit.SPADES, Rank.JACK),
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.CLUBS, Rank.NINE),
                                new Card(Suit.DIAMONDS, Rank.NINE)
                        ),
                        UpcardRecipient.PARTNER,
                        true,
                        true,
                        true
                ),
                OrderUpScenario.of(
                        "13. 9♠ to you; J♣, J♠, A♠, 9♣, 9♦ — all three call",
                        new Card(Suit.SPADES, Rank.NINE),
                        cards(
                                new Card(Suit.CLUBS, Rank.JACK),
                                new Card(Suit.SPADES, Rank.JACK),
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.CLUBS, Rank.NINE),
                                new Card(Suit.DIAMONDS, Rank.NINE)
                        ),
                        UpcardRecipient.SELF,
                        true,
                        true,
                        true
                ), OrderUpScenario.of(
                        "13. 9♥ to you; J♣, J♠, A♠, 9♣, 9♦ — none call",
                        new Card(Suit.HEARTS, Rank.NINE),
                        cards(
                                new Card(Suit.CLUBS, Rank.JACK),
                                new Card(Suit.SPADES, Rank.JACK),
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.CLUBS, Rank.NINE),
                                new Card(Suit.DIAMONDS, Rank.NINE)
                        ),
                        UpcardRecipient.SELF,
                        false,
                        false,
                        false
                ),
                OrderUpScenario.of(
                        "14. 9♠ to opponents; J♠, J♣, K♥, Q♣, 9♦ — aggressive and neutral",
                        new Card(Suit.SPADES, Rank.NINE),
                        cards(
                                new Card(Suit.SPADES, Rank.JACK),
                                new Card(Suit.CLUBS, Rank.JACK),
                                new Card(Suit.HEARTS, Rank.KING),
                                new Card(Suit.CLUBS, Rank.QUEEN),
                                new Card(Suit.DIAMONDS, Rank.NINE)
                        ),
                        UpcardRecipient.OPPONENT,
                        true,
                        true,
                        false
                ),
                OrderUpScenario.of(
                        "15. 10♠ to opponents; J♥, Q♥, J♠, Q♠, Q♦ — none",
                        new Card(Suit.SPADES, Rank.TEN),
                        cards(
                                new Card(Suit.HEARTS, Rank.JACK),
                                new Card(Suit.HEARTS, Rank.QUEEN),
                                new Card(Suit.SPADES, Rank.JACK),
                                new Card(Suit.SPADES, Rank.QUEEN),
                                new Card(Suit.DIAMONDS, Rank.QUEEN)
                        ),
                        UpcardRecipient.OPPONENT,
                        false,
                        false,
                        false
                ),
                OrderUpScenario.of(
                        "16. Q♣ to opponents; 10♣, A♣, 10♠, K♣, K♠ — aggressive and neutral",
                        new Card(Suit.CLUBS, Rank.QUEEN),
                        cards(
                                new Card(Suit.CLUBS, Rank.TEN),
                                new Card(Suit.CLUBS, Rank.ACE),
                                new Card(Suit.SPADES, Rank.TEN),
                                new Card(Suit.CLUBS, Rank.KING),
                                new Card(Suit.SPADES, Rank.KING)
                        ),
                        UpcardRecipient.OPPONENT,
                        true,
                        true,
                        false
                ),
                OrderUpScenario.of(
                        "17. A♦ to you; A♠, 10♥, 9♥, Q♠, K♦ — none",
                        new Card(Suit.DIAMONDS, Rank.ACE),
                        cards(
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.HEARTS, Rank.TEN),
                                new Card(Suit.HEARTS, Rank.NINE),
                                new Card(Suit.SPADES, Rank.QUEEN),
                                new Card(Suit.DIAMONDS, Rank.KING)
                        ),
                        UpcardRecipient.SELF,
                        false,
                        false,
                        false
                ),
                OrderUpScenario.of(
                        "18. Q♠ to opponents; Q♦, A♣, J♦, J♣, A♠ — aggressive and neutral",
                        new Card(Suit.SPADES, Rank.QUEEN),
                        cards(
                                new Card(Suit.DIAMONDS, Rank.QUEEN),
                                new Card(Suit.CLUBS, Rank.ACE),
                                new Card(Suit.DIAMONDS, Rank.JACK),
                                new Card(Suit.CLUBS, Rank.JACK),
                                new Card(Suit.SPADES, Rank.ACE)
                        ),
                        UpcardRecipient.OPPONENT,
                        true,
                        true,
                        false
                ),
                OrderUpScenario.of(
                        "19. 9♠ to opponents; A♦, J♣, A♠, J♦, Q♠ — all three call",
                        new Card(Suit.SPADES, Rank.NINE),
                        cards(
                                new Card(Suit.DIAMONDS, Rank.ACE),
                                new Card(Suit.CLUBS, Rank.JACK),
                                new Card(Suit.SPADES, Rank.ACE),
                                new Card(Suit.DIAMONDS, Rank.JACK),
                                new Card(Suit.SPADES, Rank.QUEEN)
                        ),
                        UpcardRecipient.OPPONENT,
                        true,
                        true,
                        true
                )

        );
    }

    private static void assertStrategyDecision(String strategyName,
                                               EuchreAIStrategy strategy,
                                               OrderUpScenario scenario,
                                               boolean expected) {
        assertEquals(
                expected,
                strategy.shouldOrderUp(scenario.upCard(), scenario.hand(), scenario.recipient()),
                () -> scenario.name() + " -> " + strategyName
        );
    }

    private static List<Card> cards(Card... cards) {
        return List.of(cards);
    }

    private record OrderUpScenario(
            String name,
            Card upCard,
            List<Card> hand,
            UpcardRecipient recipient,
            boolean aggressiveExpected,
            boolean neutralExpected,
            boolean conservativeExpected
    ) {
        private static OrderUpScenario of(String name,
                                          Card upCard,
                                          List<Card> hand,
                                          UpcardRecipient recipient,
                                          boolean aggressiveExpected,
                                          boolean neutralExpected,
                                          boolean conservativeExpected) {
            return new OrderUpScenario(
                    name,
                    upCard,
                    hand,
                    recipient,
                    aggressiveExpected,
                    neutralExpected,
                    conservativeExpected
            );
        }

        private static OrderUpScenario of(String name,
                                          List<Card> hand,
                                          boolean aggressiveExpected,
                                          boolean neutralExpected,
                                          boolean conservativeExpected) {
            return of(
                    name,
                    new Card(Suit.HEARTS, Rank.JACK),
                    hand,
                    UpcardRecipient.PARTNER,
                    aggressiveExpected,
                    neutralExpected,
                    conservativeExpected
            );
        }
    }
}
