package org.litebridgedb.orm.support;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;

/**
 * Automated discovery of {@link TypeSafeDtoTableMapping} mappings used by Litebridge.
 * <p>
 * A utility class for scanning base packages to locate classes extending {@link TypeSafeDtoTableMapping}
 * and registering them with a Litebridge instance.
 */
public final class TypeSafeDtoMappingScanner {

    /**
     * Scans provided base packages for classes extending {@link TypeSafeDtoTableMapping} and creates and returns instances of them.
     *
     * @param packageNames Base package(s) to scan for mapping classes
     * @return An array of instances of classes extending {@link TypeSafeDtoTableMapping}
     */
    public TypeSafeDtoTableMapping[] scanBasePackage(final String... packageNames) {
        try (final ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(packageNames)
                .scan()) {

            return scanResult.getSubclasses(TypeSafeDtoTableMapping.class).stream()
                    .map(classInfo -> {
                        final Class<?> typeSafeMappingClass = classInfo.loadClass();
                        final TypeSafeDtoTableMapping typeSafeMapping = (TypeSafeDtoTableMapping) ClassUtils.newInstance(typeSafeMappingClass);
                        return typeSafeMapping;
                    })
                    .toArray(TypeSafeDtoTableMapping[]::new);
        }
    }
}
