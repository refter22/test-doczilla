package filemerger.dependency;

class StreamingDependencyExtractorTest extends DependencyExtractorTest {
    private final DependencyExtractor extractor = new StreamingDependencyExtractor();

    @Override
    DependencyExtractor getExtractor() {
        return extractor;
    }
}