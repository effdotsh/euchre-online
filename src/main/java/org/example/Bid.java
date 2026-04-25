package org.example;


import java.util.Optional;

public final class Bid {
    public enum BidType {
        PASS,
        ORDER_UP,
        CALL_TRUMP,
        GO_ALONE
    }

    private final BidType type;
    private final Optional<Suit> trump;
    private final boolean alone;

    public static Bid pass() {
        return new Bid(BidType.PASS, Optional.empty(), false);
    }

    public static Bid orderUp(boolean alone) {
        return new Bid(BidType.ORDER_UP, Optional.empty(), alone);
    }

    public static Bid callTrump(Suit suit, boolean alone) {
        return new Bid(BidType.CALL_TRUMP, Optional.of(suit), alone);
    }

    private Bid(BidType type, Optional<Suit> trump, boolean alone) {
        this.type = type;
        this.trump = trump;
        this.alone = alone;
    }

    public BidType getType() {
        return type;
    }

    public Optional<Suit> getTrump() {
        return trump;
    }

    public boolean isAlone() {
        return alone;
    }
}