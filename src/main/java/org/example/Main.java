package org.example;

public class Main {
    public static void main(String[] args) {
        GameBuilder.BuildResult builtGame = GameBuilder.builder()
                .withSeat(GameBuilder.Seat.NORTH, GameBuilder.PlayerKind.RANDOM_AI, "Lance")
                .withSeat(GameBuilder.Seat.EAST, GameBuilder.PlayerKind.CLI, "Ephram")
                .withSeat(GameBuilder.Seat.SOUTH, GameBuilder.PlayerKind.RANDOM_AI, "Laura")
                .withSeat(GameBuilder.Seat.WEST, GameBuilder.PlayerKind.RANDOM_AI, "Olivia")
                .withStickDealer(true)
                .build();

        builtGame.game().playGame();
    }
}