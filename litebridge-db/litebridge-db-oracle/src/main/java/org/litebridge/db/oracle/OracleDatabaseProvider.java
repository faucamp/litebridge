package org.litebridge.db.oracle;

import org.jspecify.annotations.Nullable;
import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.oracle.api.LitebridgeOracle;
import org.litebridge.db.oracle.engine.OracleExecutionEngine;
import org.litebridge.db.oracle.engine.OracleInsertAllEngine;
import org.litebridge.db.oracle.function.OracleSqlFunctionRegistryFactory;
import org.litebridge.db.oracle.sql.OracleMathOperationGenerator;
import org.litebridge.db.oracle.sql.OracleSqlGenerator;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.LitebridgeOverrideDatabaseProvider;
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
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.SqlGenerator;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.config.LitebridgeConfig;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * Oracle Database Provider for Litebridge.
 */
public final class OracleDatabaseProvider extends AbstractDatabaseProvider implements LitebridgeOverrideDatabaseProvider<LitebridgeOracle> {

    /**
     * Constructs a new {@code OracleDatabaseProvider}.
     */
    public OracleDatabaseProvider() {
        super(databaseProviderContext());
    }

    private static DatabaseProviderContext databaseProviderContext() {
        final DatabaseProviderMetaData databaseProviderMetaData =
                new DatabaseProviderMetaData(true,
                        true,
                        DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS);

        final MetaDataEngine metaDataEngine = new DefaultMetaDataEngine(databaseProviderMetaData);
        final ColumnIdentifierGenerator columnIdentifierGenerator = new OracleColumnIdentifierGenerator();
        final MathOperationGenerator mathOperationGenerator = new OracleMathOperationGenerator(columnIdentifierGenerator);
        final SqlGenerator sqlGenerator = new OracleSqlGenerator(metaDataEngine, columnIdentifierGenerator, mathOperationGenerator);
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final AliasTransformer aliasTransformer = new UppercaseAliasTransformer();
        final ExecutionEngine executionEngine = new OracleExecutionEngine(typeConverter, aliasTransformer);
        final SqlFunctionRegistryFactory sqlFunctionRegistry = new OracleSqlFunctionRegistryFactory(columnIdentifierGenerator, sqlGenerator.selectSqlGenerator());

        return ContextBuilder.newContext()
                .withAliasTransformer(aliasTransformer)
                .withColumnIdentifierGenerator(columnIdentifierGenerator)
                .withMathOperationGenerator(mathOperationGenerator)
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withExecutionEngine(executionEngine)
                .withSqlFunctionRegistryFactory(sqlFunctionRegistry)
                .withSqlGenerator(sqlGenerator)
                .withTypeConverter(typeConverter)
                .withSequenceColumnValueGenerator(OracleSequenceColumnValueGenerator::new)
                .build();
    }

    @Override
    public Class<LitebridgeOracle> litebridgeClass() {
        return LitebridgeOracle.class;
    }

    @Override
    public LitebridgeOracle createLitebridge(final @Nullable Object[] constructorArgs) {
        final DatabaseProvider databaseProvider = (DatabaseProvider) Objects.requireNonNull(constructorArgs[0]);
        final TransactionManager transactionManager = (TransactionManager) Objects.requireNonNull(constructorArgs[1]);
        final LitebridgeConfig litebridgeConfig = (LitebridgeConfig) constructorArgs[2];
        final MethodHandles.Lookup lookup = (MethodHandles.Lookup) Objects.requireNonNull(constructorArgs[3]);
        final OracleSqlGenerator oracleSqlGenerator = (OracleSqlGenerator) context.sqlGenerator();
        final OracleInsertAllEngine oracleInsertAllEngine = new OracleInsertAllEngine(oracleSqlGenerator.oracleInsertSqlGenerator());

        return new LitebridgeOracle(
                databaseProvider,
                transactionManager,
                litebridgeConfig,
                lookup,
                oracleInsertAllEngine);
    }
}
