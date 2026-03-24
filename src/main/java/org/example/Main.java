package org.example;

public class Main {
    public static void main(String[] args) {
        Player[] players = {
                new HumanPlayer("Lance"),
                new HumanPlayer("Ephram"),
                new HumanPlayer("Laura"),
                new HumanPlayer("Olivia")
        };
        Euchre euchre = new Euchre(players);
        euchre.playGame();
    }
}