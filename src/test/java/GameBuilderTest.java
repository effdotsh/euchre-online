import org.example.GameBuilder;
import org.example.Players.CLIPlayer;
import org.example.Players.Player;
import org.example.Players.RandomAIPlayer;
import org.example.Players.RemotePlayer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameBuilderTest {

    @Test
    void buildAssignsPlayersToExpectedSeats() {
        GameBuilder.BuildResult result = GameBuilder.builder()
                .withSeat(GameBuilder.Seat.NORTH, GameBuilder.PlayerKind.CLI, "North Human")
                .withSeat(GameBuilder.Seat.EAST, GameBuilder.PlayerKind.RANDOM_AI, "East Bot")
                .withSeat(GameBuilder.Seat.SOUTH, GameBuilder.PlayerKind.REMOTE, "South Remote")
                .withSeat(GameBuilder.Seat.WEST, GameBuilder.PlayerKind.RANDOM_AI, "West Bot")
                .build();

        Map<GameBuilder.Seat, Player> playersBySeat = result.playersBySeat();
        assertInstanceOf(CLIPlayer.class, playersBySeat.get(GameBuilder.Seat.NORTH));
        assertInstanceOf(RandomAIPlayer.class, playersBySeat.get(GameBuilder.Seat.EAST));
        assertInstanceOf(RemotePlayer.class, playersBySeat.get(GameBuilder.Seat.SOUTH));
        assertInstanceOf(RandomAIPlayer.class, playersBySeat.get(GameBuilder.Seat.WEST));
        assertEquals("South Remote", playersBySeat.get(GameBuilder.Seat.SOUTH).getName());
    }

    @Test
    void buildFailsWhenSeatIsMissing() {
        GameBuilder builder = GameBuilder.builder()
                .withSeat(GameBuilder.Seat.NORTH, GameBuilder.PlayerKind.CLI, "North")
                .withSeat(GameBuilder.Seat.EAST, GameBuilder.PlayerKind.CLI, "East")
                .withSeat(GameBuilder.Seat.SOUTH, GameBuilder.PlayerKind.CLI, "South");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void remoteAtFailsForNonRemoteSeat() {
        GameBuilder.BuildResult result = GameBuilder.builder()
                .withSeat(GameBuilder.Seat.NORTH, GameBuilder.PlayerKind.CLI, "North")
                .withSeat(GameBuilder.Seat.EAST, GameBuilder.PlayerKind.RANDOM_AI, "East")
                .withSeat(GameBuilder.Seat.SOUTH, GameBuilder.PlayerKind.REMOTE, "South")
                .withSeat(GameBuilder.Seat.WEST, GameBuilder.PlayerKind.CLI, "West")
                .build();

        assertThrows(IllegalStateException.class, () -> result.remoteAt(GameBuilder.Seat.NORTH));
        assertEquals(1, result.remotePlayers().size());
    }

    @Test
    void builderCanDisableStickDealerRule() {
        GameBuilder.BuildResult result = GameBuilder.builder()
                .withStickDealer(false)
                .withSeat(GameBuilder.Seat.NORTH, GameBuilder.PlayerKind.RANDOM_AI, "North")
                .withSeat(GameBuilder.Seat.EAST, GameBuilder.PlayerKind.RANDOM_AI, "East")
                .withSeat(GameBuilder.Seat.SOUTH, GameBuilder.PlayerKind.RANDOM_AI, "South")
                .withSeat(GameBuilder.Seat.WEST, GameBuilder.PlayerKind.RANDOM_AI, "West")
                .build();

        assertEquals(false, result.game().isStickDealer());
    }
}


