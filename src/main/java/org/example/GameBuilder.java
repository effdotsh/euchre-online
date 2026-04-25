package org.example;

import org.example.Players.CLIPlayer;
import org.example.Players.Player;
import org.example.Players.RandomAIPlayer;
import org.example.Players.RemotePlayer;
import org.example.Players.StrategyAIPlayer;
import org.example.Strategies.StrategyFactory;
import org.example.Strategies.StrategyType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GameBuilder {
    private final EnumMap<Seat, PlayerConfig> seats = new EnumMap<>(Seat.class);
    private long actionDelayMillis;
    private boolean stickDealer = true;

    private GameBuilder() {
    }

    public static GameBuilder builder() {
        return new GameBuilder();
    }

    public GameBuilder withActionDelayMillis(long actionDelayMillis) {
        if (actionDelayMillis < 0) {
            throw new IllegalArgumentException("actionDelayMillis cannot be negative");
        }
        this.actionDelayMillis = actionDelayMillis;
        return this;
    }

    public GameBuilder withStickDealer(boolean stickDealer) {
        this.stickDealer = stickDealer;
        return this;
    }

    public GameBuilder withSeat(Seat seat, PlayerKind kind, String name) {
        Objects.requireNonNull(seat, "seat cannot be null");
        Objects.requireNonNull(kind, "kind cannot be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        seats.put(seat, new PlayerConfig(kind, name));
        return this;
    }

    public BuildResult build() {
        validateAllSeatsConfigured();

        Player[] players = new Player[Euchre.NUM_PLAYERS];
        EnumMap<Seat, Player> playersBySeat = new EnumMap<>(Seat.class);
        for (Seat seat : Seat.values()) {
            Player player = createPlayer(seats.get(seat));
            players[seat.index()] = player;
            playersBySeat.put(seat, player);
        }

        Euchre game = new Euchre(players, actionDelayMillis, stickDealer);
        return new BuildResult(game, Map.copyOf(playersBySeat));
    }

    private void validateAllSeatsConfigured() {
        List<Seat> missingSeats = java.util.Arrays.stream(Seat.values())
                .filter(seat -> !seats.containsKey(seat))
                .toList();
        if (!missingSeats.isEmpty()) {
            throw new IllegalStateException("Missing seat configuration for: " + missingSeats);
        }
    }

    private static Player createPlayer(PlayerConfig playerConfig) {
        return switch (playerConfig.kind()) {
            case CLI -> new CLIPlayer(playerConfig.name());
            case REMOTE -> new RemotePlayer(playerConfig.name());
            case RANDOM_AI -> new RandomAIPlayer(playerConfig.name());
            case STRATEGY_AI -> new StrategyAIPlayer(playerConfig.name(), StrategyFactory.create(StrategyType.NEUTRAL));
        };
    }

    public enum Seat {
        NORTH(0),
        EAST(1),
        SOUTH(2),
        WEST(3);

        private final int index;

        Seat(int index) {
            this.index = index;
        }

        int index() {
            return index;
        }
    }

    public enum PlayerKind {
        CLI,
        REMOTE,
        RANDOM_AI,
        STRATEGY_AI
    }

    private record PlayerConfig(PlayerKind kind, String name) {
    }

    public record BuildResult(Euchre game, Map<Seat, Player> playersBySeat) {
        public RemotePlayer remoteAt(Seat seat) {
            Player player = playersBySeat.get(seat);
            if (player instanceof RemotePlayer remotePlayer) {
                return remotePlayer;
            }
            throw new IllegalStateException("Seat " + seat + " is not a remote player");
        }

        public List<RemotePlayer> remotePlayers() {
            return playersBySeat.values().stream()
                    .filter(RemotePlayer.class::isInstance)
                    .map(RemotePlayer.class::cast)
                    .toList();
        }
    }
}


