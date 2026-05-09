package org.litebridge.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for Litebridge.
 */
@ConfigurationProperties(prefix = "litebridge")
public class LitebridgeProperties {

    /**
     * Fully qualified class name of the database provider implementation to use.
     */
    private String databaseProviderClass;

    /**
     * Get the fully qualified class name of the database provider implementation to use.
     *
     * @return database provider class name
     */
    public String getDatabaseProviderClass() {
        return databaseProviderClass;
    }

    /**
     * Set the fully qualified class name of the database provider implementation to use.
     */
    public void setDatabaseProviderClass(String databaseProviderClass) {
        this.databaseProviderClass = databaseProviderClass;
    }
}
