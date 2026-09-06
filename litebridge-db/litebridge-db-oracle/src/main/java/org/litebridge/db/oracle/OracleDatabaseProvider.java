package org.litebridge.db.oracle;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.oracle.engine.OracleExecutionEngine;
import org.litebridge.db.oracle.function.OracleSqlFunctionRegistryFactory;
import org.litebridge.db.oracle.sql.OracleSqlGenerator;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.ContextBuilder;
import org.litebridge.db.spi.impl.DatabaseProviderContext;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.db.spi.impl.engine.DefaultMetaDataEngine;
import org.litebridge.db.spi.impl.engine.ExecutionEngine;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.function.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.SqlGenerator;

/**
 * Oracle Database Provider for Litebridge.
 */
public final class OracleDatabaseProvider extends AbstractDatabaseProvider {

    /**
     * Constructs a new {@code OracleDatabaseProvider}.
     */
    public OracleDatabaseProvider() {
        super(databaseProviderContext());
    }

    private static DatabaseProviderContext databaseProviderContext() {
        final DatabaseProviderMetaData databaseProviderMetaData =
                new DatabaseProviderMetaData(true,
                        DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS);

        final MetaDataEngine metaDataEngine = new DefaultMetaDataEngine(databaseProviderMetaData);
        final SqlGenerator sqlGenerator = new OracleSqlGenerator(metaDataEngine);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final AliasTransformer aliasTransformer = new UppercaseAliasTransformer();
        final ExecutionEngine executionEngine = new OracleExecutionEngine(typeConverter, aliasTransformer);
        final ColumnIdentifierGenerator columnIdentifierGenerator = new OracleColumnIdentifierGenerator();
        final SqlFunctionRegistryFactory sqlFunctionRegistry = new OracleSqlFunctionRegistryFactory(columnIdentifierGenerator, sqlGenerator.selectSqlGenerator());

        return ContextBuilder.newContext()
                .withAliasTransformer(aliasTransformer)
                .withColumnIdentifierGenerator(columnIdentifierGenerator)
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withExecutionEngine(executionEngine)
                .withSqlFunctionRegistryFactory(sqlFunctionRegistry)
                .withSqlGenerator(sqlGenerator)
                .withTypeConverter(typeConverter)
                .withSequenceColumnValueGenerator(OracleSequenceColumnValueGenerator::new)
                .build();
    }
}
