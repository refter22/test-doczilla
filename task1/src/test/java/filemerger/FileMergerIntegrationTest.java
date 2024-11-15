package filemerger;

import filemerger.exceptions.CyclicDependencyException;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileMergerIntegrationTest {
    @TempDir
    Path tempDir;

    Stream<FileMerger> implementations() {
        return Stream.of(
            FileMergerFactory.createInMemoryMerger(),
            FileMergerFactory.createStreamingMerger()
        );
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldProcessExampleFromTaskDescription(FileMerger merger) throws Exception {
        // given
        Path folder1 = tempDir.resolve("Folder 1");
        Path folder2 = tempDir.resolve("Folder 2");
        Files.createDirectories(folder1);
        Files.createDirectories(folder2);

        Path file11 = folder1.resolve("File 1-1.txt");
        Path file21 = folder2.resolve("File 2-1.txt");
        Path file22 = folder2.resolve("File 2-2.txt");

        String file11Content = String.join("\n",
            "Lorem ipsum dolor sit amet",
            "*require 'Folder 2/File 2-1.txt'*",
            "Praesent feugiat egestas sem."
        );
        String file21Content = "Phasellus eget tellus ac risus";
        String file22Content = String.join("\n",
            "*require 'Folder 1/File 1-1.txt'*",
            "*require 'Folder 2/File 2-1.txt'*",
            "In pretium dictum lacinia."
        );

        Files.writeString(file11, file11Content);
        Files.writeString(file21, file21Content);
        Files.writeString(file22, file22Content);

        Path outputFile = tempDir.resolve("output.txt");

        // when
        merger.merge(tempDir.toString(), outputFile.toString());

        // then
        String result = Files.readString(outputFile);
        String[] files = result.split("\n\n");
        assertEquals(3, files.length);
        assertTrue(result.contains(file21Content.trim()));
        assertTrue(result.contains(file11Content.trim()));
        assertTrue(result.contains(file22Content.trim()));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldDetectCyclicDependencies(FileMerger merger) throws Exception {
        // given
        Path folder = tempDir.resolve("folder");
        Files.createDirectories(folder);

        Path file1 = folder.resolve("file1.txt");
        Path file2 = folder.resolve("file2.txt");

        Files.writeString(file1, "*require 'folder/file2.txt'*\ncontent1");
        Files.writeString(file2, "*require 'folder/file1.txt'*\ncontent2");

        Path outputFile = tempDir.resolve("output.txt");

        // when & then
        CyclicDependencyException exception = assertThrows(
            CyclicDependencyException.class,
            () -> merger.merge(tempDir.toString(), outputFile.toString())
        );

        String message = exception.getMessage().toLowerCase();
        assertTrue(message.contains("file1.txt"));
        assertTrue(message.contains("file2.txt"));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldHandleDeepNestedFolders(FileMerger merger) throws Exception {
        // given
        Path deep = tempDir.resolve("level1/level2/level3");
        Path other = tempDir.resolve("other");
        Files.createDirectories(deep);
        Files.createDirectories(other);

        Path deepFile = deep.resolve("deep.txt");
        Path otherFile = other.resolve("other.txt");
        Path rootFile = tempDir.resolve("root.txt");

        Files.writeString(deepFile, "*require 'other/other.txt'*\nDeep file content");
        Files.writeString(otherFile, "*require 'root.txt'*\nOther file content");
        Files.writeString(rootFile, "Root file content");

        Path outputFile = tempDir.resolve("output.txt");

        // when
        merger.merge(tempDir.toString(), outputFile.toString());

        // then
        String result = Files.readString(outputFile);
        assertTrue(result.indexOf("Root file content") < result.indexOf("Other file content"));
        assertTrue(result.indexOf("Other file content") < result.indexOf("Deep file content"));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldHandleMultipleDependencyLevels(FileMerger merger) throws Exception {
        // given
        Files.createDirectories(tempDir.resolve("folder"));
        Path fileA = tempDir.resolve("folder/A.txt");
        Path fileB = tempDir.resolve("folder/B.txt");
        Path fileC = tempDir.resolve("folder/C.txt");
        Path fileD = tempDir.resolve("folder/D.txt");

        Files.writeString(fileA, "*require 'folder/B.txt'*\nContent A");
        Files.writeString(fileB, "*require 'folder/C.txt'*\nContent B");
        Files.writeString(fileC, "*require 'folder/D.txt'*\nContent C");
        Files.writeString(fileD, "Content D");

        Path outputFile = tempDir.resolve("output.txt");

        // when
        merger.merge(tempDir.toString(), outputFile.toString());

        // then
        String result = Files.readString(outputFile);
        assertTrue(result.indexOf("Content D") < result.indexOf("Content C"));
        assertTrue(result.indexOf("Content C") < result.indexOf("Content B"));
        assertTrue(result.indexOf("Content B") < result.indexOf("Content A"));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldHandleMissingDependency(FileMerger merger) throws Exception {
        // given
        Files.createDirectories(tempDir.resolve("folder"));
        Path file = tempDir.resolve("folder/file.txt");
        Files.writeString(file, "*require 'nonexistent.txt'*\nContent");
        Path outputFile = tempDir.resolve("output.txt");

        // when & then
        assertThrows(RuntimeException.class, () ->
            merger.merge(tempDir.toString(), outputFile.toString())
        );
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldHandleComplexDependencyGraph(FileMerger merger) throws Exception {
        // given
        Files.createDirectories(tempDir.resolve("folder"));

        /*
            Граф зависимостей:
            A -> B, C
            B -> D
            C -> D
            D -> E
            E (нет зависимостей)
        */

        Path fileA = tempDir.resolve("folder/A.txt");
        Path fileB = tempDir.resolve("folder/B.txt");
        Path fileC = tempDir.resolve("folder/C.txt");
        Path fileD = tempDir.resolve("folder/D.txt");
        Path fileE = tempDir.resolve("folder/E.txt");

        Files.writeString(fileA, String.join("\n",
            "*require 'folder/B.txt'*",
            "*require 'folder/C.txt'*",
            "Content A"
        ));
        Files.writeString(fileB, "*require 'folder/D.txt'*\nContent B");
        Files.writeString(fileC, "*require 'folder/D.txt'*\nContent C");
        Files.writeString(fileD, "*require 'folder/E.txt'*\nContent D");
        Files.writeString(fileE, "Content E");

        Path outputFile = tempDir.resolve("output.txt");

        // when
        merger.merge(tempDir.toString(), outputFile.toString());

        // then
        String result = Files.readString(outputFile);

        // E должен быть первым
        assertTrue(result.indexOf("Content E") < result.indexOf("Content D"));
        // D должен быть перед B и C
        assertTrue(result.indexOf("Content D") < result.indexOf("Content B"));
        assertTrue(result.indexOf("Content D") < result.indexOf("Content C"));
        // B и C должны быть перед A
        assertTrue(result.indexOf("Content B") < result.indexOf("Content A"));
        assertTrue(result.indexOf("Content C") < result.indexOf("Content A"));
    }

    @ParameterizedTest
    @MethodSource("implementations")
    void shouldHandleEmptyFiles(FileMerger merger) throws Exception {
        // given
        Files.createDirectories(tempDir.resolve("folder"));
        Path fileA = tempDir.resolve("folder/A.txt");
        Path fileB = tempDir.resolve("folder/B.txt");

        Files.writeString(fileA, "");
        Files.writeString(fileB, "*require 'folder/A.txt'*");

        Path outputFile = tempDir.resolve("output.txt");

        // when
        merger.merge(tempDir.toString(), outputFile.toString());

        // then
        String result = Files.readString(outputFile);
        assertFalse(result.isBlank());
        assertTrue(result.contains("*require 'folder/A.txt'*"));
    }
}