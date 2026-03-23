import org.example.AIPlayer;
import org.example.Euchre;
import org.example.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EuchreTest {

    private List<Player> createPlayers() {
        return List.of(
                new AIPlayer("Blue1", new Random(1)),
                new AIPlayer("Red1", new Random(2)),
                new AIPlayer("Blue2", new Random(3)),
                new AIPlayer("Red2", new Random(4))
        );
    }

    @Test
    void gameCompletesWithoutError() {
        Euchre euchre = new Euchre(createPlayers());
        assertDoesNotThrow(euchre::playGame);
    }

}
