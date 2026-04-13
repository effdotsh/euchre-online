package org.example;

public enum Suit {
    SPADES(Color.BLACK, "♠", 0),
    DIAMONDS(Color.RED, "♦", 13),
    CLUBS(Color.BLACK, "♣", 26),
    HEARTS(Color.RED, "♥", 39);

    private final Color color;
    private final String string;
    private final int baseOrder;

    Suit(Color color, String string, int order) {
        this.color = color;
        this.string = string;
        this.baseOrder = order;
    }

    @Override
    public String toString() {
        return this.string;
    }

    public Color getColor() {
        return color;
    }

    public enum Color {
        RED,
        BLACK
    }

    public int getBaseOrder() {
        return baseOrder;
    }
}