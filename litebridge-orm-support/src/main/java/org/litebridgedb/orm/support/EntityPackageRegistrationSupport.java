package org.litebridgedb.orm.support;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.annotation.Table;

public class EntityPackageRegistrationSupport implements PackageRegistrationSupport {

    private final Litebridge litebridge;

    public EntityPackageRegistrationSupport(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    /**
     * Scans provided base packages for entity classes annotated with @Table and registers them with Litebridge.
     *
     * @param packageNames Base package(s) to scan for entity classes
     */
    @Override
    public void scanBasePackage(final String... packageNames) {
        try (final ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .acceptPackages(packageNames)
                .scan()) {

            final Class<?>[] entityClasses = scanResult.getClassesWithAnnotation(Table.class.getName()).stream()
                    .map(ClassInfo::loadClass)
                    .toArray(Class<?>[]::new);

            litebridge.register(entityClasses);
        }
    }
}
