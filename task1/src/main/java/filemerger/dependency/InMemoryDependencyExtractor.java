package filemerger.dependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class InMemoryDependencyExtractor extends AbstractDependencyExtractor {
    @Override
    public List<String> extractDependencies(String filePath, String rootPath) {
        try {
            Path fullPath = Path.of(rootPath).resolve(filePath);
            String content = Files.readString(fullPath);
            List<String> dependencies = new ArrayList<>();

            Matcher matcher = REQUIRE_PATTERN.matcher(content);

            while (matcher.find()) {
                dependencies.add(matcher.group(1));
            }

            return dependencies;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }
}
