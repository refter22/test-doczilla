package filemerger.dependency;

import java.util.List;

public interface DependencyExtractor {
    /**
     * Извлекает зависимости из файла
     * @param filePath путь к файлу
     * @param rootPath корневая директория
     * @return список путей к файлам-зависимостям
     */
    List<String> extractDependencies(String filePath, String rootPath);
}
