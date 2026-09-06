package org.litebridge.db.postgres;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ContextBuilder;
import org.litebridge.db.spi.impl.DatabaseProviderContext;

/**
 * PostgreSQL database provider for Litebridge.
 */
public final class PostgresDatabaseProvider extends AbstractDatabaseProvider {

    /**
     * Constructs a new {@code PostgresDatabaseProvider}.
     */
    public PostgresDatabaseProvider() {
        super(databaseProviderContext());
    }

    private static DatabaseProviderContext databaseProviderContext() {
        final DatabaseProviderMetaData databaseProviderMetaData =
                new DatabaseProviderMetaData(true,
                        DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS);

        return ContextBuilder.newContext()
                .withAliasTransformer(new PostgresAliasTransformer())
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withSequenceColumnValueGenerator(PostgresSequenceColumnValueGenerator::new)
                .withTypeConverter(new DefaultTypeConverter())
                .build();
    }
}
