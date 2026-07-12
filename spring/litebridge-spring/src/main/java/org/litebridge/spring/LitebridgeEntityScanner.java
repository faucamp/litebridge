package org.litebridge.spring;

import org.litebridge.orm.annotation.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Scanner for Litebridge entities.
 */
public final class LitebridgeEntityScanner extends AbstractScanner {
    /**
     * Constructs a new {@code LitebridgeEntityScanner} instance.
     */
    public LitebridgeEntityScanner() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LitebridgeEntityScanner.class);

    /**
     * Scans the given base packages for Litebridge entities.
     *
     * @param packageNames the base packages to scan
     * @return the found entity classes
     */
    public Class<?>[] scanBasePackage(final String... packageNames) {
        LOGGER.debug("Scanning base package(s) '{}' for Litebridge entities", (Object) packageNames);
        return findClasses(packageNames).toArray(Class<?>[]::new);
    }

    /**
     * Creates a {@link ClassPathScanningCandidateComponentProvider} configured to scan for
     * classes annotated with {@link Table}.
     *
     * @return the configured scanner provider
     */
    @Override
    protected ClassPathScanningCandidateComponentProvider createScanningProvider() {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Table.class));
        return provider;
    }
}
