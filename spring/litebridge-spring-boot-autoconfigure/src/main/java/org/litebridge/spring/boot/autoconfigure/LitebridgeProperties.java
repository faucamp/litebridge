package org.litebridge.spring.boot.autoconfigure;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

/**
 * Spring Boot configuration properties for Litebridge.
 */
@ConfigurationProperties(prefix = "litebridge")
public class LitebridgeProperties {

    /**
     * Database provider configuration.
     */
    private DatabaseProviderProperties databaseProvider = new DatabaseProviderProperties();

    /**
     * One or more base packages to scan for Litebridge entities or type-safe DTO mappings.
     */
    private @Nullable String[] scanBasePackage;

    /**
     * Defines how related DTOs should be handled when not included as a JOIN in a query.
     * <p>
     * This sets the Litebridge instance's default related DTO strategy.
     * It can be overriden on a per-query basis.
     * <p>
     * Default: {@link RelatedDtoStrategy#NULL_IF_NO_JOIN}
     */
    private RelatedDtoStrategy relatedDtoStrategy = RelatedDtoStrategy.NULL_IF_NO_JOIN;

    /**
     * How related DTOs should be handled when not included as a JOIN in a query.
     * <p>
     * This returns the Litebridge instance's default related DTO strategy.
     * It can be overriden on a per-query basis.
     * <p>
     * Default: {@link RelatedDtoStrategy#NULL_IF_NO_JOIN}
     *
     * @return the Litebridge instance's default related DTO strategy
     */
    public RelatedDtoStrategy getRelatedDtoStrategy() {
        return relatedDtoStrategy;
    }

    /**
     * Sets how related DTOs should be handled when not included as a JOIN in a query.
     * <p>
     * This sets the Litebridge instance's default related DTO strategy.
     * It can be overriden on a per-query basis.
     * <p>
     * Default: {@link RelatedDtoStrategy#NULL_IF_NO_JOIN}
     */
    public void setRelatedDtoStrategy(final RelatedDtoStrategy relatedDtoStrategy) {
        this.relatedDtoStrategy = relatedDtoStrategy;
    }

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
     * Get the base packages to scan for Litebridge entities or type-safe DTO mappings.
     *
     * @return base packages to scan
     */
    public @Nullable String[] getScanBasePackage() {
        return scanBasePackage;
    }

    /**
     * Set the base packages to scan for Litebridge entities or type-safe DTO mappings.
     *
     * @param scanBasePackage base packages to scan
     */
    public void setScanBasePackage(final @Nullable String[] scanBasePackage) {
        this.scanBasePackage = scanBasePackage;
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
         * The base package to scan for {@link org.litebridge.db.spi.DatabaseProvider} implementations.
         * <p>
         * Default: "org.litebridge.db"
         */
        private String scanBasePackage = "org.litebridge.db";

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
         * Get the base package to scan for {@link org.litebridge.db.spi.DatabaseProvider} implementations.
         * <p>
         * Default: "org.litebridge.db"
         */
        public String getScanBasePackage() {
            return scanBasePackage;
        }

        /**
         * Set the base package to scan for {@link org.litebridge.db.spi.DatabaseProvider} implementations.
         *
         * @param basePackage the base package to recursively scan for database providers
         */
        public void setScanBasePackage(final String basePackage) {
            this.scanBasePackage = basePackage;
        }
    }
}
