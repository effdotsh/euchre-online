import org.example.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void snapshotIncludesCurrentHandAndPlayers() {
        Euchre euchre = new Euchre(createPlayers());
        Hand hand = euchre.startNextHand();
        hand.playNextTrick();

        String snapshot = euchre.snapshot();

        assertTrue(snapshot.contains("\"game\":\"Euchre\""));
        assertTrue(snapshot.contains("\"currentHand\""));
        assertTrue(snapshot.contains("\"players\""));
        assertTrue(snapshot.contains("\"completedTricks\""));
        assertTrue(snapshot.contains("\"trickCount\""));
    }

    @Test
    void snapshotSwitchesToNewHandBeforeRemoteDecisionCompletes() throws Exception {
        Player[] players = createPlayers();
        Euchre euchre = new Euchre(players);

        Hand firstHand = euchre.startNextHand();
        firstHand.playHand();
        euchre.advance();

        BlockingOrderPlayer blockingPlayer = new BlockingOrderPlayer("Blue2");
        players[2] = blockingPlayer;

        Thread gameThread = new Thread(euchre::advance);
        gameThread.start();

        assertTrue(blockingPlayer.awaitPrompt(), "expected second hand to block on order-up decision");

        String snapshot = euchre.snapshot();
        assertTrue(snapshot.contains("\"handCount\":2"));
        String currentHandSlice = snapshot.substring(snapshot.indexOf("\"currentHand\""), snapshot.indexOf("\"handCount\""));
        assertTrue(currentHandSlice.contains("\"currentHand\":{\"started\":false,\"complete\":false"));
        assertFalse(currentHandSlice.contains("\"currentHand\":{\"started\":true,\"complete\":true"));

        blockingPlayer.release();
        gameThread.join(2000);
        assertFalse(gameThread.isAlive(), "advance should resume after releasing the blocker");
    }

    private static final class BlockingOrderPlayer extends Player {
        private final CountDownLatch prompted = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);

        private BlockingOrderPlayer(String name) {
            super(name);
        }

        @Override
        protected Card chooseCard(Suit trump, Optional<Suit> ledSuit) {
            List<Card> legalCards = getLegalCards(trump, ledSuit);
            return legalCards.getFirst();
        }

        @Override
        public boolean chooseToOrderUp(Card upCard) {
            prompted.countDown();
            try {
                if (!release.await(2, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out waiting to release blocking player");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Blocking player interrupted", e);
            }
            return false;
        }

        @Override
        public Optional<Suit> chooseToCallTrump(Suit forbiddenSuit, boolean dealerIsStuck) {
            return Arrays.stream(Suit.values()).filter(suit -> suit != forbiddenSuit).findFirst();
        }

        private boolean awaitPrompt() throws InterruptedException {
            return prompted.await(2, TimeUnit.SECONDS);
        }

        private void release() {
            release.countDown();
        }
    }

}
