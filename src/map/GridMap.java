package map;

public class GridMap {
    private int size;
    private Node[][] grid;

    public GridMap(int size) {
        this.size = size;
        grid = new Node[size][size];
        init();
    }

    private void init() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                grid[i][j] = new Node(i, j);
    }

    public void modifyTile(int x, int y, boolean walkable) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            grid[x][y].setWalkable(walkable);
        }
    }

    public Node getNode(int x, int y) {
        if (x >= 0 && x < size && y >= 0 && y < size) {
            return grid[x][y];
        }
        return null;
    }

    public int getSize() {
        return size;
    }

    public Node[][] getGrid() {
        return grid;
    }
}
