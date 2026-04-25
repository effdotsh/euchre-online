package org.example.Players;

import org.example.Bid;
import org.example.Card;
import org.example.Suit;

import java.util.*;
import java.util.stream.Collectors;

public class CLIPlayer extends Player {
    public CLIPlayer(String name) {
        super(name);
    }

    @Override
    protected Card chooseCard(Suit trump, Optional<Suit> ledSuit, List<PlayedCard> playedCards) {
        List<Card> legalCards = getLegalCards(trump, ledSuit);
        System.out.println("Choose a card to play");
        return chooseCard(legalCards);
    }

    @Override
    public Bid chooseToOrderUp(Card upCard) {
        System.out.println("Your hand is " + getHand().stream()
                .map(Card::toString)
                .collect(Collectors.joining(", ")));
        System.out.println("Do you want to order up?");
        String YES = "Yes";
        List<String> options = List.of(YES, "No");
        int optionIdx = getChoice(options);
        if (Objects.equals(options.get(optionIdx), YES)) {
            return Bid.orderUp(false);
        }
        return Bid.pass();
    }

    @Override
    public Bid chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
        List<Suit> suitOptions = Arrays.stream(Suit.values()).filter(s -> s != forbiddenSuit).toList();

        List<String> suitOptionsStrings = new ArrayList<>(suitOptions.stream().map(Suit::toString).toList());
        if (!dealerIsStuck) {
            suitOptionsStrings.add("Pass");
        }

        System.out.println("Choose if you want to call a suit");

        int suitIdx = getChoice(suitOptionsStrings);
        if (suitIdx < suitOptions.size()) {
            return Bid.callTrump(suitOptions.get(suitIdx), false);
        } else if (dealerIsStuck) {
            throw new RuntimeException("The dealer is stuck and must pick a suit");
        }

        return Bid.pass();
    }

    private Card chooseCard(List<Card> cardOptions) {
        if (cardOptions.isEmpty()) {
            throw new IllegalStateException("No legal cards available");
        }

        List<String> cardStrings = cardOptions.stream().map(Card::toString).toList();

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
