package org.litebridge.db.oracle.sql;

import org.litebridge.db.oracle.OracleColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.litebridge.db.spi.impl.sql.InsertSqlGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

/**
 * Specialized implementation of {@link DefaultSqlGenerator} for Oracle databases.
 * <p>
 * This class customizes the SQL generation components by providing Oracle-specific
 * implementations of {@link SelectSqlGenerator}, {@link InsertSqlGenerator}, and
 * {@link ColumnIdentifierGenerator}.
 */
public class OracleSqlGenerator extends DefaultSqlGenerator {

    @Override
    protected SelectSqlGenerator createSelectSqlGenerator() {
        return new OracleSelectSqlGenerator(columnIdentifierGenerator.getOrThrow(), metaDataEngine::ensureTableMetaData);
    }

    @Override
    protected InsertSqlGenerator createInsertSqlGenerator() {
        return new OracleInsertSqlGenerator(columnIdentifierGenerator.getOrThrow(), metaDataEngine::ensureTableMetaData);
    }

    @Override
    protected ColumnIdentifierGenerator createColumnIdentifierGenerator() {
        return new OracleColumnIdentifierGenerator();
    }
}
