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
            if (available.isEmpty() && hasUnprocessedNodes(processed)) {
                throw new CyclicDependencyException("Found cyclic dependency");
            }

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
        checkForCycles(graph);
        return sortTopologically(graph);
    }

    private DependencyGraph buildGraph(String[][] dependencies) {
        DependencyGraph graph = new DependencyGraph();
        Stream.of(dependencies)
            .forEach(dep -> graph.addDependency(dep[0], dep[1]));
        return graph;
    }

    private void checkForCycles(DependencyGraph graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        graph.getAllNodes().stream()
            .filter(node -> !visited.contains(node))
            .forEach(node -> {
                List<String> cycle = findCycle(node, graph, visited, recursionStack);
                if (!cycle.isEmpty()) {
                    String path = String.join(" -> ", cycle);
                    throw new CyclicDependencyException("Found cyclic dependency: " + path);
                }
            });
    }

    private List<String> findCycle(
        String node,
        DependencyGraph graph,
        Set<String> visited,
        Set<String> recursionStack
    ) {
        if (recursionStack.contains(node)) {
            return List.of(node);
        }

        if (visited.contains(node)) {
            return Collections.emptyList();
        }

        visited.add(node);
        recursionStack.add(node);

        try {
            return findCycleInNeighbors(node, graph, visited, recursionStack);
        } finally {
            recursionStack.remove(node);
        }
    }

    private List<String> findCycleInNeighbors(
        String node,
        DependencyGraph graph,
        Set<String> visited,
        Set<String> recursionStack
    ) {
        return graph.getNeighbors(node).stream()
            .map(neighbor -> findCycle(neighbor, graph, visited, recursionStack))
            .filter(cycle -> !cycle.isEmpty())
            .map(cycle -> addNodeToCycle(node, cycle))
            .findFirst()
            .orElse(Collections.emptyList());
    }

    private List<String> addNodeToCycle(String node, List<String> cycle) {
        if (cycle.get(0).equals(node)) {
            return cycle;
        }
        return Stream.concat(cycle.stream(), Stream.of(node))
            .collect(Collectors.toList());
    }

    private List<String> sortTopologically(DependencyGraph graph) {
        List<String> result = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        while (graph.hasUnprocessedNodes(processed)) {
            graph.processAvailableNodes(processed, result);
        }

        return result;
    }
}