package filemerger;

import filemerger.dependency.DependencyExtractor;
import filemerger.content.ContentMerger;
import filemerger.order.OrderResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.mockito.Mockito.*;

class FileMergerUnitTest {
    @TempDir
    Path tempDir;

    private DependencyExtractor dependencyExtractor;
    private ContentMerger contentMerger;
    private OrderResolver orderResolver;
    private FileMerger fileMerger;

    @BeforeEach
    void setUp() {
        dependencyExtractor = mock(DependencyExtractor.class);
        contentMerger = mock(ContentMerger.class);
        orderResolver = mock(OrderResolver.class);
        fileMerger = new SimpleFileMerger(dependencyExtractor, contentMerger, orderResolver);
    }

    @Test
    void shouldCallComponentsInCorrectOrder() throws Exception {
        // given
        String rootPath = tempDir.toString();
        String outputPath = tempDir.resolve("output.txt").toString();
        String file1 = "file1.txt";
        String file2 = "file2.txt";

        // Создаём тестовые файлы
        Path file1Path = tempDir.resolve(file1);
        Path file2Path = tempDir.resolve(file2);
        Files.writeString(file1Path, "content1");
        Files.writeString(file2Path, "content2");

        when(dependencyExtractor.extractDependencies(anyString(), eq(rootPath)))
            .thenReturn(List.of());
        when(orderResolver.resolve(any())).thenReturn(List.of(file1, file2));

        // when
        fileMerger.merge(rootPath, outputPath);

        // then
        InOrder inOrder = inOrder(dependencyExtractor, orderResolver, contentMerger);
        inOrder.verify(dependencyExtractor, atLeastOnce())
            .extractDependencies(anyString(), eq(rootPath));
        inOrder.verify(orderResolver).resolve(any());
        inOrder.verify(contentMerger).merge(eq(List.of(file1, file2)), eq(rootPath), eq(outputPath));
    }
}