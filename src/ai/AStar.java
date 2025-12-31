package ai;

import map.Node;
import java.util.*;

/**
 * A* Pathfinding Algorithm
 */
public class AStar {

    public List<Node> findPath(Node start, Node goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingInt(n -> n.fCost)
        );
        Set<Node> closedSet = new HashSet<>();
        Map<Node, Integer> gScore = new HashMap<>();
        
        gScore.put(start, 0);
        start.gCost = 0;
        start.hCost = heuristic(start, goal);
        start.fCost = start.gCost + start.hCost;
        start.parent = null;
        
        openSet.add(start);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.x == goal.x && current.y == goal.y) {
                return reconstructPath(current);
            }
            
            closedSet.add(current);
            
            // Check 4 neighbors (up, down, left, right)
            int[][] dirs = {{0,-1}, {0,1}, {-1,0}, {1,0}};
            for (int[] dir : dirs) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                
                // Create neighbor node (in real impl, get from GridMap)
                Node neighbor = new Node(nx, ny);
                
                if (closedSet.contains(neighbor)) continue;
                
                int tentativeG = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
                
                if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    neighbor.parent = current;
                    neighbor.gCost = tentativeG;
                    neighbor.hCost = heuristic(neighbor, goal);
                    neighbor.fCost = neighbor.gCost + neighbor.hCost;
                    gScore.put(neighbor, tentativeG);
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        return new ArrayList<>(); // No path found
    }

    private int heuristic(Node a, Node b) {
        // Manhattan distance
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private List<Node> reconstructPath(Node node) {
        List<Node> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
