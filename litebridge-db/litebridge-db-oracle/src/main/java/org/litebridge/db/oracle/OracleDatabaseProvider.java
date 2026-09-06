package org.litebridge.db.oracle;

import org.litebridge.convert.DefaultTypeConverter;
import org.litebridge.db.oracle.engine.OracleExecutionEngine;
import org.litebridge.db.oracle.function.OracleSqlFunctionRegistryFactory;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.AbstractDatabaseProvider;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.alias.UppercaseAliasTransformer;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle Database Provider for Litebridge.
 * <p>
 * {@code OracleDatabaseProvider} is a concrete implementation of {@link AbstractDatabaseProvider}
 * designed to facilitate interactions with an Oracle database.
 * <p>
 * It uses a {@link DefaultTypeConverter} for handling type conversions between
 * database values and Java data types.
 */
public final class OracleDatabaseProvider extends AbstractDatabaseProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleDatabaseProvider.class);

    /**
     * Constructs a new {@code OracleDatabaseProvider} using a default type converter.
     */
    public OracleDatabaseProvider() {
        super(new DefaultSqlGenerator(),
                new OracleExecutionEngine(
                        new DefaultTypeConverter(),
                        new UppercaseAliasTransformer()
                ));
    }

    @Override
    public SequenceColumnValueGenerator sequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
        return new OracleSequenceColumnValueGenerator(sequence);
    }

    @Override
    protected ColumnIdentifierGenerator createColumnIdentifierGenerator() {
        return new OracleColumnIdentifierGenerator();
    }

    @Override
    protected SqlFunctionRegistry createSqlFunctionRegistry() {
        return new OracleSqlFunctionRegistryFactory(columnIdentifierGenerator.getOrThrow(), sqlGenerator.selectSqlGenerator()).create();
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
