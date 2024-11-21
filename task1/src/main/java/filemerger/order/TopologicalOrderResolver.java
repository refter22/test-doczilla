package filemerger.order;

import filemerger.exceptions.CyclicDependencyException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Реализация сортировки зависимостей с помощью топологической сортировки.
 * Использует алгоритм Кана с дополнительной проверкой на циклы.
 */
public class TopologicalOrderResolver implements OrderResolver {
    private static class DependencyGraph {
        private final Map<String, Set<String>> edges = new HashMap<>();
        private final Map<String, Integer> inDegree = new HashMap<>();
        private final Set<String> allNodes = new HashSet<>();

        void addDependency(String dependent, String dependency) {
            edges.computeIfAbsent(dependency, k -> new HashSet<>()).add(dependent);
            edges.putIfAbsent(dependent, new HashSet<>());
            inDegree.merge(dependent, 1, Integer::sum);
            inDegree.putIfAbsent(dependency, 0);
            allNodes.addAll(List.of(dependent, dependency));
        }

        Set<String> getNeighbors(String node) {
            return edges.getOrDefault(node, Collections.emptySet());
        }

        Set<String> getAllNodes() {
            return allNodes;
        }

        boolean hasUnprocessedNodes(Set<String> processed) {
            return !processed.containsAll(allNodes);
        }

        TreeSet<String> getNodesWithoutDependencies(Set<String> processed) {
            return allNodes.stream()
                    .filter(node -> !processed.contains(node))
                    .filter(node -> inDegree.get(node) == 0)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        void removeDependency(String node, String dependent) {
            inDegree.merge(dependent, -1, Integer::sum);
        }

        void processAvailableNodes(Set<String> processed, List<String> result) {
            TreeSet<String> available = getNodesWithoutDependencies(processed);
            available.forEach(node -> {
                result.add(node);
                processed.add(node);
                getNeighbors(node).forEach(neighbor -> removeDependency(node, neighbor));
            });
        }
    }

    @Override
    public List<String> resolve(String[][] dependencies) {
        DependencyGraph graph = buildGraph(dependencies);
        return sortTopologically(graph);
    }

    private DependencyGraph buildGraph(String[][] dependencies) {
        DependencyGraph graph = new DependencyGraph();
        Stream.of(dependencies)
                .forEach(dep -> graph.addDependency(dep[0], dep[1]));
        return graph;
    }

    private List<String> sortTopologically(DependencyGraph graph) {
        List<String> result = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        while (graph.hasUnprocessedNodes(processed)) {
            TreeSet<String> available = graph.getNodesWithoutDependencies(processed);

            if (available.isEmpty()) {
                List<String> cycle = findCyclePath(graph, processed);
                String path = String.join(" -> ", cycle);
                throw new CyclicDependencyException("Found cyclic dependency: " + path);
            }

            graph.processAvailableNodes(processed, result);
        }
        return result;
    }

    private List<String> findCyclePath(DependencyGraph graph, Set<String> processed) {
        String start = graph.getAllNodes().stream()
                .filter(node -> !processed.contains(node))
                .findFirst()
                .get();
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        String current = start;

        while (!visited.contains(current)) {
            path.add(current);
            visited.add(current);
            current = graph.getNeighbors(current).stream()
                    .filter(n -> !processed.contains(n))
                    .findFirst()
                    .get();
        }

        path.add(current);

        int startIndex = path.indexOf(current);
        return path.subList(startIndex, path.size());
    }
}