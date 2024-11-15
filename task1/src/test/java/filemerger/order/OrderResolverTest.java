package filemerger.order;

import org.junit.jupiter.api.Test;
import filemerger.exceptions.CyclicDependencyException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

abstract class OrderResolverTest {
    abstract OrderResolver getResolver();

    @Test
    void shouldResolveEmptyDependencies() {
        // when
        List<String> result = getResolver().resolve(new String[0][]);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldResolveSingleDependency() {
        // given
        String[][] deps = {{"file1.txt", "file2.txt"}};

        // when
        List<String> result = getResolver().resolve(deps);

        // then
        assertEquals(List.of("file2.txt", "file1.txt"), result);
    }

    @Test
    void shouldResolveMultipleDependencies() {
        // given
        String[][] deps = {
            {"file1.txt", "file2.txt"},
            {"file2.txt", "file3.txt"}
        };

        // when
        List<String> result = getResolver().resolve(deps);

        // then
        assertEquals(List.of("file3.txt", "file2.txt", "file1.txt"), result);
    }

    @Test
    void shouldDetectCycle() {
        // given
        String[][] deps = {
            {"file1.txt", "file2.txt"},
            {"file2.txt", "file1.txt"}
        };

        // then
        CyclicDependencyException exception = assertThrows(
            CyclicDependencyException.class,
            () -> getResolver().resolve(deps)
        );

        assertTrue(exception.getMessage().contains("file1.txt"));
        assertTrue(exception.getMessage().contains("file2.txt"));
    }

    @Test
    void shouldResolveDependenciesInAlphabeticalOrder() {
        // given
        String[][] deps = {
            {"b.txt", "dep.txt"},
            {"a.txt", "dep.txt"},
            {"c.txt", "dep.txt"}
        };

        // when
        List<String> result = getResolver().resolve(deps);

        // then
        assertEquals(List.of("dep.txt", "a.txt", "b.txt", "c.txt"), result);
    }

    @Test
    void shouldResolveDependenciesInAlphabeticalOrderWithMultipleLevels() {
        // given
        String[][] deps = {
            {"b1.txt", "a2.txt"},
            {"a1.txt", "a2.txt"},
            {"c1.txt", "b2.txt"},
            {"d1.txt", "b2.txt"}
        };

        // when
        List<String> result = getResolver().resolve(deps);

        // then
        assertEquals(List.of("a2.txt", "b2.txt", "a1.txt", "b1.txt", "c1.txt", "d1.txt"), result);
    }
}