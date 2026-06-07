package org.litebridgedb.orm.support;

/**
 * Support for scanning Java packages for registering multiple entities or DTO-table mappings.
 */
public interface PackageRegistrationSupport {

    /**
     * Scan the specified base packages for Litebridge entities/DTO-table mappings.
     *
     * @param packageNames Base package(s) to scan for mapping classes
     */
    void scanBasePackage(String... packageNames);
}
