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
     * in Euchre rules, the top card is the jack of trump (Right Bower). the next top card in the jack of the same color (Left Bower).
     * The next best card is the ace of the suit lead, following that suit in descending order.
     *
     * @param trump The trump suit for the game
     * @param lead  The suit lead by the first player
     * @return the priority of card. The highest priority card takes the trick
     */
    public int getPriority(Suit trump, Suit lead) {
        if (suit == trump && rank == Rank.JACK) {
            return Priority.RIGHT_BOWER.getValue();
        }
        if (suit.getColor() == trump.getColor() && rank == Rank.JACK) {
            return Priority.LEFT_BOWER.getValue();
        }
        if (suit == trump) {
            return this.rank.getValue() + Priority.TRUMP.getValue();
        }

        if (suit == lead) {
            return this.rank.getValue();
        }

        return 0;
    }

    public Suit getEffectiveSuit(Suit trump) {
        if (trump != null
                && rank == Rank.JACK
                && suit != trump
                && suit.getColor() == trump.getColor()) {
            return trump;
        }
        return suit;
    }

    private enum Priority {
        RIGHT_BOWER(100),
        LEFT_BOWER(99),
        TRUMP(50);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        return rank.toString() + suit.toString();
    }
}


