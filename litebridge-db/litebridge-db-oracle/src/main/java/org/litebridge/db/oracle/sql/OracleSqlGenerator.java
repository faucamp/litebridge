package org.litebridge.db.oracle.sql;

import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.litebridge.db.spi.impl.sql.InsertSqlGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

/**
 * SQL generator for Oracle.
 * <p>
 * This class customises the SQL generation components by providing Oracle-specific
 * implementations of {@link SelectSqlGenerator} and {@link InsertSqlGenerator}.
 *
 * @see OracleSelectSqlGenerator
 * @see OracleInsertSqlGenerator
 */
public final class OracleSqlGenerator extends DefaultSqlGenerator {

    private final ConcurrentLazy<OracleInsertSqlGenerator> oracleInsertSqlGenerator = new ConcurrentLazy<>(() -> new OracleInsertSqlGenerator(
            columnIdentifierGenerator,
            mathOperationGenerator,
            metaDataEngine::ensureTableMetaData));

    public OracleSqlGenerator(final MetaDataEngine metaDataEngine,
                              final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final MathOperationGenerator mathOperationGenerator) {
        super(metaDataEngine, columnIdentifierGenerator, mathOperationGenerator);
    }

    @Override
    protected SelectSqlGenerator createSelectSqlGenerator() {
        return new OracleSelectSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                metaDataEngine::ensureTableMetaData);
    }

    @Override
    protected InsertSqlGenerator createInsertSqlGenerator() {
        return oracleInsertSqlGenerator.getOrThrow();
    }

    public OracleInsertSqlGenerator oracleInsertSqlGenerator() {
        return oracleInsertSqlGenerator.getOrThrow();
    }
}
