package org.example;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class CLIPlayer extends Player {
    public CLIPlayer(String name) {
        super(name);
    }

    protected Card chooseCardToPlay(Suit trump, Optional<Suit> ledSuit) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        return chooseCard(legalCards);
    }

    @Override
    public boolean chooseToOrderUp(Card upCard) {
        return false;
    }

    @Override
    public Suit chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
        return null;
    }

    private Card chooseCard(List<Card> cardOptions) {
        if (cardOptions.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }

        List<String> cardStrings = cardOptions.stream().map(Card::toString).toList();

        System.out.println("Choose a card to play");
        int choice = getChoice(cardStrings);

        return cardOptions.get(choice);
    }

    private static int getChoice(List<String> options) {
        for (int i = 0; i < options.size(); i++) {
            System.out.println("[" + i + "] " + options.get(i));
        }

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        return choice;
    }


}
