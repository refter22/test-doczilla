package filemerger;

import filemerger.dependency.InMemoryDependencyExtractor;
import filemerger.dependency.StreamingDependencyExtractor;
import filemerger.content.InMemoryContentMerger;
import filemerger.content.StreamingContentMerger;
import filemerger.order.TopologicalOrderResolver;

public class FileMergerFactory {
    /**
     * Создает in-memory реализацию FileMerger.
     * Рекомендуется использовать для небольших проектов.
     */
    public static FileMerger createInMemoryMerger() {
        return new SimpleFileMerger(
                new InMemoryDependencyExtractor(),
                new InMemoryContentMerger(),
                new TopologicalOrderResolver());
    }

    /**
     * Создает потоковую реализацию FileMerger.
     * Рекомендуется использовать для больших проектов или когда важно потребление
     * памяти.
     */
    public static FileMerger createStreamingMerger() {
        return new SimpleFileMerger(
                new StreamingDependencyExtractor(),
                new StreamingContentMerger(),
                new TopologicalOrderResolver());
    }
}