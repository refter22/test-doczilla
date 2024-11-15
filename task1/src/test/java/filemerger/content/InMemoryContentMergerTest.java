package filemerger.content;

class InMemoryContentMergerTest extends ContentMergerTest {
    private final ContentMerger merger = new InMemoryContentMerger();

    @Override
    ContentMerger getMerger() {
        return merger;
    }
}