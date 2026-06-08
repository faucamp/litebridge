package org.litebridgedb.spring;

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.Arrays;
import java.util.stream.Stream;

abstract sealed class AbstractScanner permits LitebridgeEntityScanner, LitebridgeTypeSafeDtoMappingScanner {

    protected Stream<Class<?>> findClasses(final String... packageNames) {
        return Arrays.stream(packageNames)
                .flatMap(this::findClasses);
    }

    private Stream<Class<?>> findClasses(final String basePackage) {
        final ClassPathScanningCandidateComponentProvider provider = createScanningProvider();
        return provider.findCandidateComponents(basePackage).stream()
                .map(beanDefinition -> {
                    try {
                        return Class.forName(beanDefinition.getBeanClassName());
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("Failed to load class for Litebridge entity: " + beanDefinition.getBeanClassName());
                    }
                });
    }

    protected abstract ClassPathScanningCandidateComponentProvider createScanningProvider();
}
