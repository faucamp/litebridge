package org.litebridgedb.orm.support;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.litebridgedb.orm.annotation.Table;

/**
 * Automated discovery of entity mappings used by Litebridge.
 * <p>
 * A utility class for scanning base packages to locate Litebridge entity classes annotated
 * with {@code @Table} from a specified base package.
 *
 * @see org.litebridgedb.orm.annotation.Table
 */
public final class EntityScanner {

    /**
     * Scans provided base packages for entity classes annotated with @Table and returns them.
     *
     * @param packageNames Base package(s) to scan for entity classes
     * @return An array of entity classes annotated with @Table
     */
    public Class<?>[] scanBasePackage(final String... packageNames) {
        try (final ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(packageNames)
                .scan()) {

            return scanResult.getClassesWithAnnotation(Table.class.getName()).stream()
                    .map(ClassInfo::loadClass)
                    .toArray(Class<?>[]::new);
        }
    }
}
