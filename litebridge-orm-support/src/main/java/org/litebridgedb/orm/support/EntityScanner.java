package org.litebridgedb.orm.support;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.annotation.Table;

public final class EntityScanner {

    private final Litebridge litebridge;

    public EntityScanner(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    /**
     * Scans provided base packages for entity classes annotated with @Table and registers them with Litebridge.
     *
     * @param packageNames Base package(s) to scan for entity classes
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
