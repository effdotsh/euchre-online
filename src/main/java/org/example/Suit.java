package org.example;

public enum Suit {
    HEARTS(Color.RED),
    DIAMONDS(Color.RED),
    CLUBS(Color.BLACK),
    SPADES(Color.BLACK);

    private final Color color;

    Suit(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public enum Color {
        RED,
        BLACK
    }
}