package filemerger.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class ContentMergerTest {
    @TempDir
    Path tempDir;

    abstract ContentMerger getMerger();

    @Test
    void shouldMergeEmptyList() throws Exception {
        // given
        Path outputFile = tempDir.resolve("output.txt");

        // when
        getMerger().merge(List.of(), tempDir.toString(), outputFile.toString());

        // then
        assertTrue(Files.exists(outputFile));
        assertEquals("", Files.readString(outputFile));
    }

    @Test
    void shouldMergeSingleFile() throws Exception {
        // given
        createFile("input.txt", "content");
        Path outputFile = tempDir.resolve("output.txt");

        // when
        getMerger().merge(
            List.of("input.txt"),
            tempDir.toString(),
            outputFile.toString()
        );

        // then
        assertEquals("content", Files.readString(outputFile));
    }

    @Test
    void shouldMergeMultipleFiles() throws Exception {
        // given
        createFile("file1.txt", "content1");
        createFile("file2.txt", "content2");
        Path outputFile = tempDir.resolve("output.txt");

        // when
        getMerger().merge(
            List.of("file1.txt", "file2.txt"),
            tempDir.toString(),
            outputFile.toString()
        );

        // then
        assertEquals("content1\n\ncontent2", Files.readString(outputFile));
    }

    @Test
    void shouldThrowWhenInputFileNotFound() {
        // given
        Path outputFile = tempDir.resolve("output.txt");

        // when & then
        assertThrows(RuntimeException.class, () ->
            getMerger().merge(
                List.of("nonexistent.txt"),
                tempDir.toString(),
                outputFile.toString()
            )
        );
    }

    @Test
    void shouldThrowWhenOutputDirectoryNotFound() throws Exception {
        // given
        createFile("input.txt", "content");
        Path nonexistentDir = tempDir.resolve("nonexistent");
        Path outputFile = nonexistentDir.resolve("output.txt");

        // when & then
        assertThrows(RuntimeException.class, () ->
            getMerger().merge(
                List.of("input.txt"),
                tempDir.toString(),
                outputFile.toString()
            )
        );
    }

    protected Path createFile(String name, String content) throws Exception {
        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file;
    }
}