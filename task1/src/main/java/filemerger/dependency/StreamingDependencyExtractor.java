package filemerger.dependency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Потоковая реализация извлечения зависимостей.
 *
 * - Подходит для файлов любого размера
 * - Может быть медленнее на маленьких файлах из-за накладных расходов
 */
public class StreamingDependencyExtractor implements DependencyExtractor {
    private static final Pattern REQUIRE_PATTERN = Pattern.compile("require '([^']*)'");

    @Override
    public List<String> extractDependencies(String filePath, String rootPath) {
        List<String> dependencies = new ArrayList<>();
        Path path = Path.of(rootPath).resolve(filePath);

        try {
            String content = Files.readString(path);
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
