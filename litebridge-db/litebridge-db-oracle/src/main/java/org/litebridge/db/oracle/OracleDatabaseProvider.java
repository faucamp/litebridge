package org.litebridge.db.oracle;

import org.litebridge.db.oracle.api.LitebridgeOracle;
import org.litebridge.db.oracle.engine.OracleExecutionEngine;
import org.litebridge.db.oracle.engine.OracleInsertAllEngine;
import org.litebridge.db.oracle.expression.function.OracleSqlFunctionRegistryFactory;
import org.litebridge.db.oracle.sql.OracleLabelGenerator;
import org.litebridge.db.oracle.sql.OracleMathOperationGenerator;
import org.litebridge.db.oracle.sql.OracleSqlGenerator;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ContextBuilder;
import org.litebridge.db.spi.impl.DatabaseProviderContext;
import org.litebridge.db.spi.impl.engine.ExecutionEngine;
import org.litebridge.db.spi.impl.expression.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.SqlGenerator;
import org.litebridge.orm.LitebridgeBuilder;
import org.litebridge.orm.spi.LitebridgeOverrideDatabaseProvider;

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
                        DatabaseProviderMetaData.MergeCapability.USING_VALUES_SUBQUERY,
                        DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS);
        final LabelGenerator labelGenerator = new OracleLabelGenerator();
        final MathOperationGenerator mathOperationGenerator = new OracleMathOperationGenerator(labelGenerator);

        final ContextBuilder contextBuilder = ContextBuilder.newContext()
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withLabelGenerator(labelGenerator)
                .withMathOperationGenerator(mathOperationGenerator);

        final SqlGenerator sqlGenerator = new OracleSqlGenerator(contextBuilder.ensureMetaDataEngine(), labelGenerator, mathOperationGenerator);
        final ExecutionEngine executionEngine = new OracleExecutionEngine(contextBuilder.ensureTypeConverter());
        final SqlFunctionRegistryFactory sqlFunctionRegistry = new OracleSqlFunctionRegistryFactory(labelGenerator, sqlGenerator.selectSqlGenerator());

        return contextBuilder
                .withExecutionEngine(executionEngine)
                .withSqlFunctionRegistryFactory(sqlFunctionRegistry)
                .withSqlGenerator(sqlGenerator)
                .withSequenceColumnValueGenerator(OracleSequenceColumnValueGenerator::new)
                .build();
    }

    @Override
    public Class<LitebridgeOracle> litebridgeClass() {
        return LitebridgeOracle.class;
    }

    @Override
    public LitebridgeOracle createLitebridge(final LitebridgeBuilder.ConstructorArgs constructorArgs) {
        final OracleSqlGenerator oracleSqlGenerator = (OracleSqlGenerator) context.sqlGenerator();
        final OracleInsertAllEngine oracleInsertAllEngine = new OracleInsertAllEngine(oracleSqlGenerator.oracleInsertSqlGenerator());

        return new LitebridgeOracle(
                constructorArgs.databaseProvider(),
                constructorArgs.transactionManager(),
                constructorArgs.litebridgeConfig(),
                constructorArgs.lookup(),
                oracleInsertAllEngine);
    }
}
