package org.example.Strategies;

import java.util.Objects;

public final class StrategyFactory {
    private StrategyFactory() {
    }

    public static EuchreAIStrategy create(StrategyType type) {
        Objects.requireNonNull(type, "type cannot be null");
        return switch (type) {
            case AGGRESSIVE -> new AggressiveStrategy();
            case NEUTRAL -> new NeutralStrategy();
            case CONSERVATIVE -> new ConservativeStrategy();
        };
    }
}

