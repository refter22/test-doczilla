package filemerger;

/**
 * Интерфейс для объединения текстовых файлов с учетом их зависимостей
 */
public interface FileMerger {
    /**
     * Объединяет текстовые файлы с учетом их зависимостей
     * @param rootPath путь к корневой директории с файлами
     * @param outputPath путь к выходному файлу
     * @throws filemerger.exceptions.CyclicDependencyException если найдена циклическая зависимость
     * @throws RuntimeException если файл не найден или возникла ошибка при чтении/записи
     */
    void merge(String rootPath, String outputPath);
}