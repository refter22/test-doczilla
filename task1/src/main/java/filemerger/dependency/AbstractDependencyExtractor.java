package filemerger.dependency;

import java.util.regex.Pattern;

public abstract class AbstractDependencyExtractor implements DependencyExtractor {
    protected static final Pattern REQUIRE_PATTERN = Pattern.compile("require '([^']*)'");
}
