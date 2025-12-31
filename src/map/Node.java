package map;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
