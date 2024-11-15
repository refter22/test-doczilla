package filemerger;

import filemerger.dependency.InMemoryDependencyExtractor;
import filemerger.dependency.StreamingDependencyExtractor;
import filemerger.content.InMemoryContentMerger;
import filemerger.content.StreamingContentMerger;
import filemerger.order.TopologicalOrderResolver;

public class FileMergerFactory {
    public static FileMerger createInMemoryMerger() {
        return new SimpleFileMerger(
            new InMemoryDependencyExtractor(),
            new InMemoryContentMerger(),
            new TopologicalOrderResolver()
        );
    }

    public static FileMerger createStreamingMerger() {
        return new SimpleFileMerger(
            new StreamingDependencyExtractor(),
            new StreamingContentMerger(),
            new TopologicalOrderResolver()
        );
    }
}