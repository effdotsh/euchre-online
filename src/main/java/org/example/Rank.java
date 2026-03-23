package org.example;


public enum Rank {
    ACE(14, "A"),
    KING(13, "K"),
    QUEEN(12, "Q"),
    JACK(11, "J"),
    TEN(10, "10"),
    NINE(9, "9");

    private final int value;
    private final String string;

    Rank(int value, String string) {
        this.value = value;
        this.string = string;
    }

    @Override
    public String toString() {
        return this.string;
    }

    public int getValue() {
        return value;
    }
}