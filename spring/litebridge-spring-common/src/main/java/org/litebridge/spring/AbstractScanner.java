package org.litebridge.spring;

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Abstract base class for classpath scanners.
 */
abstract sealed class AbstractScanner permits LitebridgeEntityScanner {

    /**
     * Finds classes in the given base packages.
     *
     * @param packageNames the base packages to scan
     * @return a stream of found classes
     */
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

    /**
     * Creates the scanning provider.
     *
     * @return the scanning provider
     */
    protected abstract ClassPathScanningCandidateComponentProvider createScanningProvider();
}
