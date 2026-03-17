package org.example;

public class Card {
    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    /**
     * in Euchre rules, the top card is the jack of trump (Left Bower). the next top card in the jack of the same color (Right Bower).
     * The next best card is the ace of the suit lead, following that suit in descending order.
     *
     * @param trump The trump suit for the game
     * @param lead  The suit lead by the first player
     * @return the priority of card. The highest priority card takes the trick
     */
    public int getPriority(Suit trump, Suit lead) {
        if (suit == trump && rank == Rank.JACK) {
            int LEFT_BOWER_PRIORITY = 100;
            return LEFT_BOWER_PRIORITY;
        }
        if (suit.getColor() == trump.getColor() && rank == Rank.JACK) {
            int RIGHT_BOWER_PRIORITY = 99;
            return RIGHT_BOWER_PRIORITY;
        }

        if (suit == trump) {
            int TRUMP_BONUS = 50;
            return this.rank.getValue() + TRUMP_BONUS;
        }

        if (suit == lead) {
            return this.rank.getValue();
        }

        return 0;
    }
}
