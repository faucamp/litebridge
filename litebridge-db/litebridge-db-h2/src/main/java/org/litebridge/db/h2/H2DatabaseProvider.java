package org.litebridge.db.h2;

import org.litebridge.db.h2.expression.function.H2SqlFunctionRegistryFactory;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ContextBuilder;
import org.litebridge.db.spi.impl.DatabaseProviderContext;
import org.litebridge.db.spi.impl.expression.SqlFunctionRegistryFactory;

/**
 * H2 database provider for Litebridge.
 */
public final class H2DatabaseProvider extends AbstractDatabaseProvider {

    /**
     * Creates a new {@code H2DatabaseProvider}.
     */
    public H2DatabaseProvider() {
        super(databaseProviderContext());
    }

    private static DatabaseProviderContext databaseProviderContext() {
        final DatabaseProviderMetaData databaseProviderMetaData =
                new DatabaseProviderMetaData(true,
                        DatabaseProviderMetaData.MergeCapability.USING_VALUES,
                        DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);

        ContextBuilder context = ContextBuilder.newContext()
                .withDatabaseProviderMetaData(databaseProviderMetaData);

        final SqlFunctionRegistryFactory sqlFunctionRegistryFactory
                = new H2SqlFunctionRegistryFactory(context.ensureLabelGenerator(), context.ensureSqlGenerator().selectSqlGenerator());

        return context.withSqlFunctionRegistryFactory(sqlFunctionRegistryFactory)
                .build();
    }
}
