package filemerger.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryContentMerger implements ContentMerger {
    @Override
    public void merge(List<String> sortedPaths, String rootPath, String outputPath) {
        try {
            String result = sortedPaths.stream()
                .map(path -> {
                    try {
                        return Files.readString(Path.of(rootPath, path));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read file: " + path, e);
                    }
                })
                .collect(Collectors.joining("\n\n"));

            Files.writeString(Path.of(outputPath), result);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write output file: " + outputPath, e);
        }
    }
}
