import org.example.Euchre;
import org.example.Player;
import org.example.RandomAIPlayer;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EuchreTest {

    private Player[] createPlayers() {
        return new Player[]{
                new RandomAIPlayer("Blue1", new Random(1)),
                new RandomAIPlayer("Red1", new Random(2)),
                new RandomAIPlayer("Blue2", new Random(3)),
                new RandomAIPlayer("Red2", new Random(4))
        };
    }

    @Test
    void gameCompletesWithoutError() {
        Euchre euchre = new Euchre(createPlayers());
        assertDoesNotThrow(euchre::playGame);
    }

}
