package filemerger.dependency;

class InMemoryDependencyExtractorTest extends DependencyExtractorTest {
    private final DependencyExtractor extractor = new InMemoryDependencyExtractor();

    @Override
    DependencyExtractor getExtractor() {
        return extractor;
    }
}