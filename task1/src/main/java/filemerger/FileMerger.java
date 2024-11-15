package filemerger;

import filemerger.dependency.DependencyExtractor;
import filemerger.content.ContentMerger;
import filemerger.order.OrderResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FileMerger {
    private final DependencyExtractor dependencyExtractor;
    private final ContentMerger contentMerger;
    private final OrderResolver orderResolver;

    public FileMerger(
        DependencyExtractor dependencyExtractor,
        ContentMerger contentMerger,
        OrderResolver orderResolver
    ) {
        this.dependencyExtractor = dependencyExtractor;
        this.contentMerger = contentMerger;
        this.orderResolver = orderResolver;
    }

    public void merge(String rootPath, String outputPath) {
        List<String> files = findTextFiles(rootPath);

        List<String[]> dependencies = files.stream()
            .flatMap(file -> toDependencyPairs(file, rootPath))
            .toList();

        List<String> sorted = orderResolver.resolve(dependencies.toArray(new String[0][]));

        contentMerger.merge(sorted, rootPath, outputPath);
    }

    private List<String> findTextFiles(String rootPath) {
        try {
            Path root = Path.of(rootPath).toAbsolutePath().normalize();
            System.out.println("Searching for .txt files in: " + root);

            try (Stream<Path> walk = Files.walk(root)) {
                List<String> files = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .sorted()
                    .toList();

                System.out.println("Found files: " + files);
                return files;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find text files in: " + rootPath, e);
        }
    }

    private Stream<String[]> toDependencyPairs(String file, String rootPath) {
        return dependencyExtractor.extractDependencies(
            Path.of(rootPath, file).toString(),
            rootPath
        )
        .stream()
        .map(dep -> new String[]{file, dep});
    }
}