package map;

import java.io.Serializable;

public class Node implements Serializable {
    public int x;
    public int y;
    private boolean walkable = true;

    // A* pathfinding fields
    public int gCost;
    public int hCost;
    public int fCost;
    public Node parent;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }
}
