package org.example;

public enum Suit {
    HEARTS(Color.RED, "♥"),
    DIAMONDS(Color.RED, "♦"),
    CLUBS(Color.BLACK, "♣"),
    SPADES(Color.BLACK, "♠");

    private final Color color;
    private final String string;

    Suit(Color color, String string) {
        this.color = color;
        this.string = string;
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
}