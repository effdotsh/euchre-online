package org.example;

import java.util.List;
import java.util.Scanner;

public class CLIPlayer extends Player {
    public CLIPlayer(String name) {
        super(name);
    }

    protected Card chooseCardToPlay(Suit trump, Suit ledSuit) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        return chooseCard(legalCards);
    }

    private Card chooseCard(List<Card> cardOptions) {
        if (cardOptions.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }

        System.out.println("You have the following cards");
        for (int i = 0; i < cardOptions.size(); i++) {
            System.out.println("[" + i + "] " + cardOptions.get(i));
        }

        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        return cardOptions.get(choice);
    }
}
