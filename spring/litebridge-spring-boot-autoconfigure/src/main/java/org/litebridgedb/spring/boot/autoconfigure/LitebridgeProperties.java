package org.litebridgedb.spring.boot.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

/**
 * Spring Boot configuration properties for Litebridge.
 */
@ConfigurationProperties(prefix = "litebridgedb")
public class LitebridgeProperties {

    /**
     * Database provider configuration.
     */
    private DatabaseProviderProperties databaseProvider = new DatabaseProviderProperties();

    /**
     * Get the database provider configuration.
     *
     * @return database provider configuration
     */
    public DatabaseProviderProperties getDatabaseProvider() {
        return databaseProvider;
    }

    /**
     * Set the database provider configuration.
     *
     * @param databaseProvider database provider configuration
     */
    public void setDatabaseProvider(final DatabaseProviderProperties databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    /**
     * Database provider configuration properties.
     */
    public static class DatabaseProviderProperties {

        /**
         * Fully qualified class name of the database provider implementation to use.
         */
        @Name("class")
        private @Nullable String providerClass;

        /**
         * The base package to scan for {@link org.litebridgedb.db.spi.DatabaseProvider} implementations.
         * <p>
         * Default: "org.litebridgedb.db"
         */
        private String scanBasePackage = "org.litebridgedb.db";

        /**
         * Get the fully qualified class name of the database provider implementation to use.
         *
         * @return database provider class name
         */
        public @Nullable String getProviderClass() {
            return providerClass;
        }

        /**
         * Set the fully qualified class name of the database provider implementation to use.
         */
        public void setProviderClass(final @Nullable String providerClass) {
            this.providerClass = providerClass;
        }

        /**
         * Alias for {@link #setProviderClass(String)}, for compatibility with Spring Boot 3 due to lack of support for {@code @Name}.
         * <p>
         * Set the fully qualified class name of the database provider implementation to use.
         */
        public void setClass(final @Nullable String providerClass) {
            this.providerClass = providerClass;
        }

        /**
         * Get the base package to scan for {@link org.litebridgedb.db.spi.DatabaseProvider} implementations.
         * <p>
         * Default: "org.litebridgedb.db"
         */
        public String getScanBasePackage() {
            return scanBasePackage;
        }

        /**
         * Set the base package to scan for {@link org.litebridgedb.db.spi.DatabaseProvider} implementations.
         *
         * @param basePackage the base package to recursively scan for database providers
         */
        public void setScanBasePackage(final String basePackage) {
            this.scanBasePackage = basePackage;
        }
    }
}
