package org.example.strategies;

import org.example.Card;
import org.example.Suit;
import org.example.UpcardRecipient;

import java.util.List;
import java.util.Optional;

public interface EuchreAIStrategy {

    boolean shouldOrderUp(Card upCard, List<Card> hand, UpcardRecipient upcardRecipient);

    Optional<Suit> chooseCallTrump(Suit forbiddenSuit, List<Card> hand);

}

