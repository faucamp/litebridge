package org.litebridge.orm;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.spi.LitebridgeOverrideDatabaseProvider;
import org.litebridge.orm.tx.DefaultTransactionManager;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;

/**
 * A builder class for constructing Litebridge instances.
 * <p>
 * The constructed Litebridge instance type depends on the capabilities of the specified database provider.
 * Some database providers may provide a specialised Litebridge implementation that supports
 * additional vendor-specific features, or they may limit the default Litebridge API due to lack of support for
 * features like SQL {@code MERGE INTO} operations (in which case a {@link LitebridgeCore} instance is returned).
 * <p>
 * This builder provides a fluent API for configuring dependencies and optional parameters
 * used during the instantiation of the Litebridge instance.
 *
 * @param <LB> The specific subtype of {@link LitebridgeCore} this builder will construct based on the database provider.
 */
public final class LitebridgeBuilder<LB extends LitebridgeCore> {

    private final Class<LB> litebridgeClass;
    private final DatabaseProvider databaseProvider;
    private final DataSource dataSource;
    private @Nullable TransactionManager transactionManager;
    private @Nullable LitebridgeConfig config;
    private MethodHandles.@Nullable Lookup lookup;

    /**
     * Creates a new {@code LitebridgeBuilder} instance for the specified database provider and data source.
     * <p>
     * The Litebridge type constructed by this builder is {@link Litebridge}.
     *
     * @param databaseProvider The database provider responsible for database interactions.
     * @param dataSource       The data source for database connections.
     */
    LitebridgeBuilder(final DatabaseProvider databaseProvider, final DataSource dataSource) {
        this.litebridgeClass = (Class<LB>) Litebridge.class;
        this.databaseProvider = databaseProvider;
        this.dataSource = dataSource;
    }

    /**
     * Creates a new {@code LitebridgeBuilder} and overrides the constructed Litebridge instance type using the
     * type provided by the specified Litebridge-overriding database provider.
     *
     * @param databaseProvider The database provider responsible for database interactions.
     * @param dataSource       The data source for database connections.
     */
    LitebridgeBuilder(final LitebridgeOverrideDatabaseProvider<LB> databaseProvider, final DataSource dataSource) {
        this.litebridgeClass = databaseProvider.litebridgeClass();
        this.databaseProvider = databaseProvider;
        this.dataSource = dataSource;
    }

    /**
     * Sets the transaction manager to be used by the Litebridge instance.
     *
     * @param transactionManager the transaction manager to use
     * @return this builder instance for method chaining
     */
    public LitebridgeBuilder<LB> withTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        return this;
    }

    /**
     * Sets the Litebridge configuration to be used by the Litebridge instance.
     *
     * @param config the Litebridge configuration to use
     * @return this builder instance for method chaining
     */
    public LitebridgeBuilder<LB> withConfig(final LitebridgeConfig config) {
        this.config = config;
        return this;
    }

    /**
     * Sets the method handle lookup to be used by the Litebridge instance.
     *
     * @param lookup the method handle lookup to use
     * @return this builder instance for method chaining
     */
    public LitebridgeBuilder<LB> withLookup(final MethodHandles.Lookup lookup) {
        this.lookup = lookup;
        return this;
    }

    /**
     * Constructs the Litebridge instance based on the configuration provided to this builder.
     * <p>
     * The constructed Litebridge instance type depends on the capabilities of the specified database provider.
     * Some database providers may provide a specialised Litebridge implementation that supports
     * additional vendor-specific features, or they may limit the default Litebridge API due to lack of support for
     * features like SQL {@code MERGE INTO} operations (in which case a {@link LitebridgeCore} instance is returned).
     *
     * @return the constructed Litebridge instance
     */
    @SuppressWarnings("unchecked")
    public LB build() {
        final TransactionManager finalTransactionManager = transactionManager != null ? transactionManager : new DefaultTransactionManager(dataSource);
        final LitebridgeConfig finalLitebridgeConfig = config != null ? config : new LitebridgeConfig();
        final MethodHandles.Lookup finalLookup = lookup != null ? lookup : MethodHandles.lookup();

        if (litebridgeClass.equals(Litebridge.class)) {
            // Default Litebridge
            return (LB) new Litebridge(
                    databaseProvider,
                    finalTransactionManager,
                    finalLitebridgeConfig,
                    finalLookup);

        }

        if (litebridgeClass.equals(LitebridgeCore.class)) {
            // Basic Litebridge (no merge support)
            return (LB) new LitebridgeCore(
                    databaseProvider,
                    finalTransactionManager,
                    finalLitebridgeConfig,
                    finalLookup);
        }

        // Custom database-specific Litebridge instance; Let the database provider instantiate it
        final LitebridgeOverrideDatabaseProvider<LB> litebridgeOverrideDatabaseProvider = (LitebridgeOverrideDatabaseProvider<LB>) databaseProvider;

        final ConstructorArgs constructorArgs = new ConstructorArgs(
                databaseProvider,
                finalTransactionManager,
                finalLitebridgeConfig,
                finalLookup);

        return litebridgeOverrideDatabaseProvider.createLitebridge(constructorArgs);
    }

    /**
     * Constructor arguments for a custom database provider-provided Litebridge instance.
     * <p>
     * This allows the database provider to instantiate a custom Litebridge instance,
     * allowing it to extend the core Litebridge API.
     */
    public record ConstructorArgs(DatabaseProvider databaseProvider,
                                  TransactionManager transactionManager,
                                  LitebridgeConfig litebridgeConfig,
                                  MethodHandles.Lookup lookup) {

    }
}
