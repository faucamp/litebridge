package org.litebridge.db.sqlite;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ContextBuilder;
import org.litebridge.db.spi.impl.DatabaseProviderContext;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.db.sqlite.engine.SQLiteExecutionEngine;
import org.litebridge.db.sqlite.engine.SQLiteMetaDataEngine;

/**
 * SQLite database provider for Litebridge.
 */
public final class SQLiteDatabaseProvider extends AbstractDatabaseProvider {

    /**
     * Constructs a new instance of {@code SQLiteDatabaseProvider}.
     */
    public SQLiteDatabaseProvider() {
        super(databaseProviderContext());
    }

    /**
     * SQLite does not support sequences. Throws an {@code UnsupportedOperationException} if called.
     *
     * @param sequence the sequence name
     * @return N/A; throws an {@code UnsupportedOperationException}
     * @throws UnsupportedOperationException if called
     */
    @Override
    public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("SQLite does not support sequences");
    }

    private static DatabaseProviderContext databaseProviderContext() {
        final DatabaseProviderMetaData databaseProviderMetaData =
                new DatabaseProviderMetaData(false,
                        DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS);

        final TypeConverter typeConverter = new DefaultTypeConverter();
        final AliasTransformer aliasTransformer = new UppercaseAliasTransformer();
        final SQLiteExecutionEngine executionEngine = new SQLiteExecutionEngine(typeConverter, aliasTransformer);

        return ContextBuilder.newContext()
                .withAliasTransformer(aliasTransformer)
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withExecutionEngine(executionEngine)
                .withMetaDataEngine(new SQLiteMetaDataEngine())
                .withTypeConverter(typeConverter)
                .build();
    }
}
