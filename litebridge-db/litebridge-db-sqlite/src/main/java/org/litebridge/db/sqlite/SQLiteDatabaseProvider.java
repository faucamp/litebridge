package org.litebridge.db.sqlite;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.orm.spi.LitebridgeOverrideDatabaseProvider;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.ContextBuilder;
import org.litebridge.db.spi.impl.DatabaseProviderContext;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.sqlite.engine.SQLiteExecutionEngine;
import org.litebridge.db.sqlite.engine.SQLiteMetaDataEngine;
import org.litebridge.db.sqlite.sql.SQLiteSqlGenerator;
import org.litebridge.orm.LitebridgeCore;

/**
 * SQLite database provider for Litebridge.
 */
public final class SQLiteDatabaseProvider extends AbstractDatabaseProvider implements LitebridgeOverrideDatabaseProvider<LitebridgeCore> {

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
                        false,
                        DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS);

        final TypeConverter typeConverter = new DefaultTypeConverter();
        final AliasTransformer aliasTransformer = new UppercaseAliasTransformer();
        final SQLiteExecutionEngine executionEngine = new SQLiteExecutionEngine(typeConverter, aliasTransformer);
        final MetaDataEngine metaDataEngine = new SQLiteMetaDataEngine();
        final ColumnIdentifierGenerator columnIdentifierGenerator = new ColumnIdentifierGenerator();
        final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(columnIdentifierGenerator);
        final SQLiteSqlGenerator sqlGenerator = new SQLiteSqlGenerator(metaDataEngine, columnIdentifierGenerator, mathOperationGenerator);

        return ContextBuilder.newContext()
                .withAliasTransformer(aliasTransformer)
                .withColumnIdentifierGenerator(columnIdentifierGenerator)
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withExecutionEngine(executionEngine)
                .withMathOperationGenerator(mathOperationGenerator)
                .withMetaDataEngine(metaDataEngine)
                .withSqlGenerator(sqlGenerator)
                .withTypeConverter(typeConverter)
                .build();
    }

    @Override
    public Class<LitebridgeCore> litebridgeClass() {
        return LitebridgeCore.class;
    }
}
