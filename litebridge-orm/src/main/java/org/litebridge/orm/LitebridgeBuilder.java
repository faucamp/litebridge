package org.litebridge.orm;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.LitebridgeOverrideDatabaseProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.tx.DefaultTransactionManager;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;

public final class LitebridgeBuilder<LB extends LitebridgeCore> {

    private final Class<LB> litebridgeClass;
    private final DatabaseProvider databaseProvider;
    private final DataSource dataSource;
    private @Nullable TransactionManager transactionManager;
    private @Nullable LitebridgeConfig config;
    private MethodHandles.@Nullable Lookup lookup;

    LitebridgeBuilder(final DatabaseProvider databaseProvider, final DataSource dataSource) {
        this.litebridgeClass = (Class<LB>) Litebridge.class;
        this.databaseProvider = databaseProvider;
        this.dataSource = dataSource;
    }

    LitebridgeBuilder(final LitebridgeOverrideDatabaseProvider<LB> databaseProvider, final DataSource dataSource) {
        this.litebridgeClass = databaseProvider.litebridgeClass();
        this.databaseProvider = databaseProvider;
        this.dataSource = dataSource;
    }

    public LitebridgeBuilder<LB> withTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        return this;
    }

    public LitebridgeBuilder<LB> withConfig(final LitebridgeConfig config) {
        this.config = config;
        return this;
    }

    public LitebridgeBuilder<LB> withLookup(final MethodHandles.Lookup lookup) {
        this.lookup = lookup;
        return this;
    }

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

        final Object[] constructorArgs = new Object[]{
                databaseProvider,
                finalTransactionManager,
                finalLitebridgeConfig,
                finalLookup};

        return litebridgeOverrideDatabaseProvider.createLitebridge(constructorArgs);
    }
}
