package filemerger.content;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Потоковая реализация слияния содержимого файлов.
 *
 * - Подходит для любого количества и размера файлов
 * - Может быть медленнее на маленьких файлах из-за накладных расходов
 */
public class StreamingContentMerger implements ContentMerger {
    @Override
    public void merge(List<String> files, String rootPath, String outputPath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputPath))) {
            boolean first = true;
            for (String file : files) {
                if (!first) {
                    writer.write("\n\n");
                }
                String content = Files.readString(Path.of(rootPath, file)).trim();
                writer.write(content);
                first = false;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to merge files", e);
        }
    }
}