# EUCHRE ONLINE

Contributing members' names:

    Names: Lance Kluge, Ephram Cukier
    Java Version: 23

A Java project that models core pieces of the card game Euchre.

This README provides a short overview of what is implemented so far and next steps/plans around our patterns and what
not.

## What we have so far

- Card model (`Card`, `Rank`, `Suit`) including Euchre-specific rules for the left/right bowers and effective suit
  handling.
- Player abstraction (`Player`) with two concrete implementations:
    - `AIPlayer` — a simple random-play AI
    - `HumanPlayer` — a placeholder human player implementation
- `Euchre` — the start of a game controller that manages the tricks and the scoring
- `Hand` — the hand playable logic, keeps track of trump, who called, the tricks taken and so on
- `Main` — example entry point that prints test output of all random players.

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

Testing is over 14 unit tests at this moment, and we are continuing to strive for tdd as we flesh out basic
functionality for the actual game engine part of our euchre game.

## Current Patterns:

- Simple Factory Pattern — `Card.createDeck()`  creation of a standard Euchre deck (iterates `Suit` and `Rank` and
  returns a `List<Card>`). This hides the construction details, makes the deck creation reusable, and simplifies testing
  by keeping creation logic in one place.

- Observer Pattern (planned) — we plan to implement an observer/event system in the game engine so UI components can
  subscribe to game events (hand dealt, trick played, score updated). This should make it easy to add both a desktop UI
  and, later, a web-based frontend. Note: observer pattern and its classes have not been implemented yet but are clearly
  on the schedule later down the line.

- Template Pattern — implemented in the `Player` abstract class. The final `playCard` method serves as the template,
  keeping the common sequence (choosing a card then removing it from the hand), while deferring the selection logic to
  the `chooseCard` method implemented by subclasses like `AIPlayer` and `HumanPlayer`.

## Next steps / TODO

- Complete the bidding phase logic (`chooseToOrderUp` and `chooseToCallTrump`) across all player types.
- Add support for a player "going alone" during the game flow and scoring.
- Improve `HumanPlayer` by adding interactive console or GUI input.
- Implement the Observer Pattern to connect the core game engine (`Euchre`) to future UI/Web frontends.
- Add more unit tests and integration tests for complete game rules.




