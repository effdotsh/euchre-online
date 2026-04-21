import org.example.strategies.AggressiveStrategy;
import org.example.strategies.ConservativeStrategy;
import org.example.strategies.NeutralStrategy;
import org.example.strategies.StrategyFactory;
import org.example.strategies.StrategyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StrategyFactoryTest {

    @Test
    void createReturnsAggressiveStrategy() {
        assertInstanceOf(AggressiveStrategy.class, StrategyFactory.create(StrategyType.AGGRESSIVE));
    }

    @Test
    void createReturnsNeutralStrategy() {
        assertInstanceOf(NeutralStrategy.class, StrategyFactory.create(StrategyType.NEUTRAL));
    }

    @Test
    void createReturnsConservativeStrategy() {
        assertInstanceOf(ConservativeStrategy.class, StrategyFactory.create(StrategyType.CONSERVATIVE));
    }

    @Test
    void createThrowsForNullType() {
        assertThrows(NullPointerException.class, () -> StrategyFactory.create(null));
    }
}

