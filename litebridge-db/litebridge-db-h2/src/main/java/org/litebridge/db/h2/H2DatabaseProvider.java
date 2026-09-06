package org.litebridge.db.h2;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ContextBuilder;
import org.litebridge.db.spi.impl.DatabaseProviderContext;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;

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
                        DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW);

        return ContextBuilder.newContext()
                .withAliasTransformer(new UppercaseAliasTransformer())
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withTypeConverter(new DefaultTypeConverter())
                .build();
    }
}
