package org.litebridge.db.sqlite.sql;

import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

/**
 * SQL generator for SQLite.
 * <p>
 * This class customises the SQL generation components by providing a SQLite-specific
 * implementation of {@link SelectSqlGenerator}.
 *
 * @see SQLiteSelectSqlGenerator
 */
public class SQLiteSqlGenerator extends DefaultSqlGenerator {

    public SQLiteSqlGenerator(final MetaDataEngine metaDataEngine,
                              final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final MathOperationGenerator mathOperationGenerator) {
        super(metaDataEngine, columnIdentifierGenerator, mathOperationGenerator);
    }

    @Override
    protected SelectSqlGenerator createSelectSqlGenerator() {
        return new SQLiteSelectSqlGenerator(
                columnIdentifierGenerator,
                mathOperationGenerator,
                metaDataEngine::ensureTableMetaData);
    }
}
