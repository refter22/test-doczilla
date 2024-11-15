package filemerger.content;

class StreamingContentMergerTest extends ContentMergerTest {
    private final ContentMerger merger = new StreamingContentMerger();

    @Override
    ContentMerger getMerger() {
        return merger;
    }
}