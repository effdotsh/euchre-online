# EUCHRE ONLINE

Contributing members' names:

    Names: Lance Kluge, Ephram Cukier
    Java Version: 23

A Java project that models core pieces of the card game Euchre.

This README provides an overview of what we implemented for the Euchre game that is playable through a local port or the
CLI.

## What we built

- Card model (`Card`, `Rank`, `Suit`) including Euchre-specific rules for the left/right bowers and effective suit
  handling.
- Deck Model (`Deck`) that has create deck that is already shuffled
- Player abstraction (`Player`) with four concrete implementations:
    - `RandomAIPlayer` — a simple random-play AI
    - `StrategyAIPlayer` — an AI implementation of Player that uses different strategies depending on the score of the
      game
    - `CLIPlayer` — a human implementation of Player were the player views and makes game decisions through the CLI.
    - `RemotePlayer` — a human implementation of Player were the player views and makes game decisions through the
      websocket UI.
- Strategies abstraction (`EuchreAIStrategy`) with three concrete implementations:
    - `AggressiveStrategy` — The most aggressive in calling on trump while also taking into account offsuit Kings
    - `NeutralStrategy` — The base strategy used when the game is close/tied, makes sure to always call on two same
      colored jacks as well as 4 trump
    - `ConservativeStrategy` — The least aggressive in calling on trump while not looking at anything other than trump
      and offsuit aces
- `Euchre` — the game controller that manages the tricks and the scoring
- `Hand` — the hand playable logic, keeps track of trump, who called, the tricks taken and so on
- `Main` — example entry point that prints test output of all random players with a CLI player.
- `GameServer` — entry point for the local HTTP server that hosts the web UI and game API.

## Run

You can run the `Main` class from your IDE, or from the command line after building:

```bash
./gradlew build
java -cp build/classes/java/main org.example.Main
```

## Tests

Run unit tests with:

```bash
./gradlew test
```

Testing is over 80 unit tests at this moment with method coverage of non UI classes hovering around 86 percent (TODO
MAKE THIS 100!)

## Design Patterns:

- Simple Factory Pattern — `Card.createDeck()`  creation of a standard Euchre deck (iterates `Suit` and `Rank` and
  returns a `List<Card>`). This hides the construction details, makes the deck creation reusable, and simplifies testing
  by keeping creation logic in one place. We also have a simple factory for Strategies called `StrategyFactory`.

- OTHER PATTERN TO BE ANNOUNCED

- Strategy Pattern (planned) — We have implemented the StrategyAIPlayer which utilizes different strategies described
  above.
  The strategies are varied at runtime for each StrategyAIPlayer based on the score that the AI player is either winning
  by or loosing by.

- Template Pattern — implemented in the `Player` abstract class. The final `playCard` method serves as the template,
  keeping the common sequence (choosing a card then removing it from the hand), while deferring the selection logic to
  the `chooseCard` method implemented by subclasses like `AIPlayer` and `HumanPlayer`.




