package filemerger.content;

import java.util.List;

public interface ContentMerger {
    /**
     * Объединяет файлы в указанном порядке
     * @param sortedPaths отсортированный список путей к файлам
     * @param rootPath корневая директория
     * @param outputPath путь для выходного файла
     */
    void merge(List<String> sortedPaths, String rootPath, String outputPath);
}
