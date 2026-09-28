package org.litebridge.db.postgres;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.postgres.expression.function.PostgresSqlFunctionRegistryFactory;
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
                        true,
                        DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS);

        final ContextBuilder contextBuilder = ContextBuilder.newContext()
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withAliasTransformer(new PostgresAliasTransformer())
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withSequenceColumnValueGenerator(PostgresSequenceColumnValueGenerator::new)
                .withTypeConverter(new DefaultTypeConverter());

        final PostgresSqlFunctionRegistryFactory postgresSqlFunctionRegistryFactory =
                new PostgresSqlFunctionRegistryFactory(contextBuilder.ensureLabelGenerator(),
                        contextBuilder.ensureSqlGenerator().selectSqlGenerator());

        return contextBuilder
                .withSqlFunctionRegistryFactory(postgresSqlFunctionRegistryFactory)
                .build();
    }
}
