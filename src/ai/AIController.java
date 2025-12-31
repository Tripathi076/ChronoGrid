package ai;

import map.GridMap;
import map.Node;
import java.util.List;
import java.util.Random;

/**
 * AI Controller with pathfinding and behavior logic.
 */
public class AIController {

    private GridMap map;
    private AStar pathfinder;
    private int aiX;
    private int aiY;
    private int targetX;
    private int targetY;
    private List<Node> currentPath;
    private Random random;
    private int[][] heatMap;

    public AIController(GridMap map) {
        this.map = map;
        this.pathfinder = new AStar();
        this.random = new Random();
        this.heatMap = new int[map.getSize()][map.getSize()];
        
        // Start AI at random position
        aiX = random.nextInt(map.getSize());
        aiY = random.nextInt(map.getSize());
        pickNewTarget();
    }

    public void update() {
        // Record position in heat map
        if (aiX >= 0 && aiX < map.getSize() && aiY >= 0 && aiY < map.getSize()) {
            heatMap[aiX][aiY]++;
        }

        // Move towards target
        if (currentPath != null && !currentPath.isEmpty()) {
            Node next = currentPath.remove(0);
            aiX = next.x;
            aiY = next.y;
        } else {
            // Pick new target and calculate path
            pickNewTarget();
            calculatePath();
        }
    }

    private void pickNewTarget() {
        // Pick random walkable target
        do {
            targetX = random.nextInt(map.getSize());
            targetY = random.nextInt(map.getSize());
        } while (map.getNode(targetX, targetY) == null || 
                 !map.getNode(targetX, targetY).isWalkable());
    }

    private void calculatePath() {
        Node start = map.getNode(aiX, aiY);
        Node goal = map.getNode(targetX, targetY);
        if (start != null && goal != null) {
            currentPath = pathfinder.findPath(start, goal);
        }
    }

    // Chase player behavior
    public void chasePlayer(int playerX, int playerY) {
        targetX = playerX;
        targetY = playerY;
        calculatePath();
    }

    // Patrol behavior
    public void patrol() {
        if (currentPath == null || currentPath.isEmpty()) {
            pickNewTarget();
            calculatePath();
        }
    }

    public GridMap getMap() { return map; }
    public AStar getPathfinder() { return pathfinder; }
    public int getAIX() { return aiX; }
    public int getAIY() { return aiY; }
    public int[][] getHeatMap() { return heatMap; }
    public List<Node> getCurrentPath() { return currentPath; }
}
