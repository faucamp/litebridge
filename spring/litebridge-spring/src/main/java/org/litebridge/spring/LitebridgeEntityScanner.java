package org.litebridge.spring;

import org.litebridge.orm.annotation.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public final class LitebridgeEntityScanner extends AbstractScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LitebridgeEntityScanner.class);

    public Class<?>[] scanBasePackage(final String... packageNames) {
        LOGGER.debug("Scanning base package(s) '{}' for Litebridge entities", (Object) packageNames);
        return findClasses(packageNames).toArray(Class<?>[]::new);
    }

    protected ClassPathScanningCandidateComponentProvider createScanningProvider() {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Table.class));
        return provider;
    }
}
