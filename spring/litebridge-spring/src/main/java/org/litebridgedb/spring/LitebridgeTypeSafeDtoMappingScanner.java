package org.litebridgedb.spring;

import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

public final class LitebridgeTypeSafeDtoMappingScanner extends AbstractScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LitebridgeTypeSafeDtoMappingScanner.class);

    public TypeSafeDtoTableMapping[] scanBasePackage(final String... packageNames) {
        LOGGER.debug("Scanning base package(s) '{}' for Litebridge type-safe DTO mappings", (Object) packageNames);
        return findClasses(packageNames)
                .map(BeanUtils::instantiateClass)
                .filter(TypeSafeDtoTableMapping.class::isInstance)
                .toArray(TypeSafeDtoTableMapping[]::new);
    }

    protected ClassPathScanningCandidateComponentProvider createScanningProvider() {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(TypeSafeDtoTableMapping.class));
        return provider;
    }
}
