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
- `GameBuilder` — The builder used to configure and create games of Euchre with different options
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

Testing is over 90 unit tests at the time of submission with method coverage of non UI methods at around 99 percent.
There is one ghost method
that claims to be not covered in StrategyAIPlayer but even after extensive debugging the coverage tool showed no method
with
red lines indicating a method that is not covered so we gave up and put this disclaimer here.

## Design Patterns:

- Simple Factory Pattern — `Card.createDeck()`  creation of a standard Euchre deck (iterates `Suit` and `Rank` and
  returns a `List<Card>`). This hides the construction details, makes the deck creation reusable, and simplifies testing
  by keeping creation logic in one place. We also have a simple factory for Strategies called `StrategyFactory`.

- Builder Pattern — `GameBuilder` is used to create any instance of a game where you can place different types of
  players
  in different seats and then vary whether or not you want to play with options in euchre like Stick the Dealer. Makes
  it a lot more
  loosly coupled to the creation of a game.

- Strategy Pattern — We have implemented the StrategyAIPlayer which utilizes different strategies described
  above.
  The strategies are varied at runtime for each StrategyAIPlayer based on the score that the AI player is either winning
  by or loosing by.

- Template Pattern — implemented in the `Player` abstract class. The final `playCard` method serves as the template,
  keeping the common sequence (choosing a card then removing it from the hand), while deferring the selection logic to
  the `chooseCard` method implemented by subclasses like `AIPlayer` and `HumanPlayer`.

## AI Disclosure:

We utilized AI to do pretty much all front end work in terms of UI. That is located in the src/main/resources path
and it works quite well when you start to play through the game server.


