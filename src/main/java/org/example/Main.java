package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Player> players = List.of(new HumanPlayer("Lance"),
                new HumanPlayer("Ephram"),
                new HumanPlayer("Laura"),
                new HumanPlayer("Olivia"));
        Euchre euchre = new Euchre(players);
        euchre.playGame();
    }
}