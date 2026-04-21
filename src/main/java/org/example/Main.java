package org.example;

import org.example.Players.CLIPlayer;
import org.example.Players.Player;
import org.example.Players.RandomAIPlayer;

public class Main {
    public static void main(String[] args) {
        Player[] players = {
                new RandomAIPlayer("Lance"),
                new CLIPlayer("Ephram"),
                new RandomAIPlayer("Laura"),
                new RandomAIPlayer("Olivia")
        };
        Euchre euchre = new Euchre(players);
        euchre.playGame();
    }
}