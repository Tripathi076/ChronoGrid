package game;

import player.Player;
import map.GridMap;
import java.util.logging.Logger;

/**
 * Multiplayer Manager - handles 3 players on different timelines.
 * Player 1 → Past
 * Player 2 → Present
 * Player 3 → Future
 */
public class MultiplayerManager {
    private static final Logger logger = Logger.getLogger(MultiplayerManager.class.getName());

    private Player pastPlayer;
    private Player presentPlayer;
    private Player futurePlayer;
    
    private Thread pastThread;
    private Thread presentThread;
    private Thread futureThread;

    public MultiplayerManager(GridMap map) {
        pastPlayer = new Player("PastPlayer", map, "PAST");
        presentPlayer = new Player("PresentPlayer", map, "PRESENT");
        futurePlayer = new Player("FuturePlayer", map, "FUTURE");
        
        // Set different starting positions
        pastPlayer.setPosition(5, 5);
        presentPlayer.setPosition(15, 15);
        futurePlayer.setPosition(25, 25);
    }

    public void startAll() {
        pastThread = new Thread(pastPlayer, "Past-Thread");
        presentThread = new Thread(presentPlayer, "Present-Thread");
        futureThread = new Thread(futurePlayer, "Future-Thread");
        
        pastThread.start();
        presentThread.start();
        futureThread.start();
        
        logger.info("All player threads started!");
    }

    public void stopAll() {
        pastPlayer.stop();
        presentPlayer.stop();
        futurePlayer.stop();
        
        try {
            pastThread.join(1000);
            presentThread.join(1000);
            futureThread.join(1000);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("All player threads stopped.");
    }

    public Player getPastPlayer() { return pastPlayer; }
    public Player getPresentPlayer() { return presentPlayer; }
    public Player getFuturePlayer() { return futurePlayer; }
}
