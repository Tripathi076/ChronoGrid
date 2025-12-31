package dsa;

import java.util.*;

public class Graph<T> {
    private Map<T, List<Edge<T>>> adjacencyList;
    private boolean directed;

    public static class Edge<T> {
        private T destination;
        private int weight;

        public Edge(T destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }

        public T getDestination() {
            return destination;
        }

        public int getWeight() {
            return weight;
        }
    }

    public Graph() {
        this(false);
    }

    public Graph(boolean directed) {
        this.adjacencyList = new HashMap<>();
        this.directed = directed;
    }

    public void addVertex(T vertex) {
        adjacencyList.putIfAbsent(vertex, new ArrayList<>());
    }

    public void addEdge(T source, T destination, int weight) {
        addVertex(source);
        addVertex(destination);
        
        adjacencyList.get(source).add(new Edge<>(destination, weight));
        
        if (!directed) {
            adjacencyList.get(destination).add(new Edge<>(source, weight));
        }
    }

    public void addEdge(T source, T destination) {
        addEdge(source, destination, 1);
    }

    public List<Edge<T>> getNeighbors(T vertex) {
        return adjacencyList.getOrDefault(vertex, Collections.emptyList());
    }

    public Set<T> getVertices() {
        return adjacencyList.keySet();
    }

    public List<T> bfs(T start) {
        List<T> result = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        Queue<T> queue = new LinkedList<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            T current = queue.poll();
            result.add(current);

            for (Edge<T> edge : getNeighbors(current)) {
                if (!visited.contains(edge.getDestination())) {
                    visited.add(edge.getDestination());
                    queue.offer(edge.getDestination());
                }
            }
        }

        return result;
    }

    public List<T> dfs(T start) {
        List<T> result = new ArrayList<>();
        Set<T> visited = new HashSet<>();
        dfsHelper(start, visited, result);
        return result;
    }

    private void dfsHelper(T vertex, Set<T> visited, List<T> result) {
        visited.add(vertex);
        result.add(vertex);

        for (Edge<T> edge : getNeighbors(vertex)) {
            if (!visited.contains(edge.getDestination())) {
                dfsHelper(edge.getDestination(), visited, result);
            }
        }
    }

    public Map<T, Integer> dijkstra(T start) {
        Map<T, Integer> distances = new HashMap<>();
        PriorityQueue<Map.Entry<T, Integer>> pq = new PriorityQueue<>(
            Comparator.comparingInt(Map.Entry::getValue)
        );

        for (T vertex : adjacencyList.keySet()) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        pq.offer(new AbstractMap.SimpleEntry<>(start, 0));

        while (!pq.isEmpty()) {
            Map.Entry<T, Integer> entry = pq.poll();
            T current = entry.getKey();
            int currentDist = entry.getValue();

            if (currentDist > distances.get(current)) {
                continue;
            }

            for (Edge<T> edge : getNeighbors(current)) {
                int newDist = currentDist + edge.getWeight();
                if (newDist < distances.get(edge.getDestination())) {
                    distances.put(edge.getDestination(), newDist);
                    pq.offer(new AbstractMap.SimpleEntry<>(edge.getDestination(), newDist));
                }
            }
        }

        return distances;
    }
}
