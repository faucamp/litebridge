package org.litebridge.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "litebridge")
public class LitebridgeProperties {

    /**
     * Fully qualified class name of the database provider implementation to use.
     */
    private String databaseProviderClass;

    public String getDatabaseProviderClass() {
        return databaseProviderClass;
    }

    public void setDatabaseProviderClass(String databaseProviderClass) {
        this.databaseProviderClass = databaseProviderClass;
    }
}
