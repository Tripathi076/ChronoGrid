package map;

import java.util.Random;

/**
 * Procedural map generator using DFS/randomized algorithms.
 */
public class MapGenerator {
    private Random random;

    public MapGenerator() {
        this.random = new Random();
    }

    public MapGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generate a grid map of given size.
     */
    public GridMap generate(int size) {
        return new GridMap(size);
    }

    /**
     * Generate a random map with obstacles.
     */
    public GridMap generateRandomMap(int width, int height, double obstacleRatio) {
        GridMap map = new GridMap(width);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (random.nextDouble() < obstacleRatio) {
                    map.getNode(x, y).setWalkable(false);
                }
            }
        }
        
        // Ensure start and end are walkable
        map.getNode(0, 0).setWalkable(true);
        map.getNode(width - 1, height - 1).setWalkable(true);
        
        return map;
    }
}
