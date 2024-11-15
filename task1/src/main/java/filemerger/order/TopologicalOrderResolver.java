package filemerger.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import filemerger.exceptions.CyclicDependencyException;

public class TopologicalOrderResolver implements OrderResolver {
    @Override
    public List<String> resolve(String[][] dependencies) {
        Map<String, Set<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Set<String> allNodes = new HashSet<>();

        for (String[] dep : dependencies) {
            String dependent = dep[0];
            String dependency = dep[1];
            graph.computeIfAbsent(dependency, k -> new HashSet<>()).add(dependent);
            graph.putIfAbsent(dependent, new HashSet<>());
            inDegree.merge(dependent, 1, Integer::sum);
            inDegree.putIfAbsent(dependency, 0);
            allNodes.add(dependent);
            allNodes.add(dependency);
        }

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : allNodes) {
            if (!visited.contains(node)) {
                List<String> cycle = findCycle(node, graph, visited, recursionStack);
                if (!cycle.isEmpty()) {
                    throw new CyclicDependencyException(
                        "Found cyclic dependency: " + String.join(" -> ", cycle)
                    );
                }
            }
        }

        List<String> result = new ArrayList<>();
        Set<String> processed = new HashSet<>();

        while (!processed.containsAll(allNodes)) {
            TreeSet<String> available = new TreeSet<>();

            for (String node : allNodes) {
                if (!processed.contains(node) && inDegree.get(node) == 0) {
                    available.add(node);
                }
            }

            if (available.isEmpty()) {
                throw new CyclicDependencyException("Found cyclic dependency");
            }

            for (String node : available) {
                result.add(node);
                processed.add(node);

                for (String neighbor : graph.get(node)) {
                    inDegree.merge(neighbor, -1, Integer::sum);
                }
            }
        }

        return result;
    }

    private List<String> findCycle(
        String node,
        Map<String, Set<String>> graph,
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

        for (String neighbor : graph.get(node)) {
            List<String> cycle = findCycle(neighbor, graph, visited, recursionStack);
            if (!cycle.isEmpty()) {
                List<String> result = new ArrayList<>(cycle);
                if (!cycle.get(0).equals(node)) {
                    result.add(node);
                }
                return result;
            }
        }

        recursionStack.remove(node);
        return Collections.emptyList();
    }
}