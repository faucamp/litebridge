package org.litebridge.db.spi;

/**
 * Information about the connected database and its JDBC driver.
 *
 * @param database Connected database metadata.
 * @param driver   JDBC driver metadata.
 */
public record DatabaseMetaData(Component database, Component driver) {

    /**
     * Database metadata component.
     *
     * @param name         Product name.
     * @param version      Product version.
     * @param majorVersion Major version number.
     * @param minorVersion Minor version number.
     */
    public record Component(String name, String version, int majorVersion, int minorVersion) {
    }
}
