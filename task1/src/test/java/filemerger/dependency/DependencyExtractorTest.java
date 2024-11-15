package filemerger.dependency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class DependencyExtractorTest {
    @TempDir
    Path tempDir;

    abstract DependencyExtractor getExtractor();

    @Test
    void shouldExtractNoDependenciesFromEmptyFile() throws Exception {
        // given
        Path file = createFile("empty.txt", "");

        // when
        List<String> dependencies = getExtractor().extractDependencies(
            file.toString(),
            tempDir.toString()
        );

        // then
        assertTrue(dependencies.isEmpty());
    }

    @Test
    void shouldExtractSingleDependency() throws Exception {
        // given
        Path file = createFile("file.txt", "require 'dep.txt'");

        // when
        List<String> dependencies = getExtractor().extractDependencies(
            file.toString(),
            tempDir.toString()
        );

        // then
        assertEquals(List.of("dep.txt"), dependencies);
    }

    @Test
    void shouldExtractMultipleDependencies() throws Exception {
        // given
        Path file = createFile("file.txt",
            "require 'dep1.txt'\n" +
            "some other content\n" +
            "require 'dep2.txt'"
        );

        // when
        List<String> dependencies = getExtractor().extractDependencies(
            file.toString(),
            tempDir.toString()
        );

        // then
        assertEquals(List.of("dep1.txt", "dep2.txt"), dependencies);
    }

    @Test
    void shouldIgnoreInvalidRequireLines() throws Exception {
        // given
        Path file = createFile("file.txt",
            "require 'valid.txt'\n" +
            "require invalid.txt\n" + // без кавычек
            "require"  // без файла
        );

        // when
        List<String> dependencies = getExtractor().extractDependencies(
            file.toString(),
            tempDir.toString()
        );

        // then
        assertEquals(List.of("valid.txt"), dependencies);
    }

    @Test
    void shouldThrowWhenFileNotFound() {
        // when & then
        assertThrows(RuntimeException.class, () ->
            getExtractor().extractDependencies(
                "nonexistent.txt",
                tempDir.toString()
            )
        );
    }

    protected Path createFile(String name, String content) throws Exception {
        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file;
    }
}