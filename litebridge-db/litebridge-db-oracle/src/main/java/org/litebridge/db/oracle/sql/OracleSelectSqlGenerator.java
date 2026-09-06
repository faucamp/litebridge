package org.litebridge.db.oracle.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.util.function.BiFunction;

public class OracleSelectSqlGenerator extends SelectSqlGenerator {

    public OracleSelectSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator, final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, ensureTableMetaData);
    }

    @Override
    protected void appendLimitClause(final Limit limit, final StringBuilder sql) {
        if (limit.offset() != null) {
            sql.append(" OFFSET ").append(limit.offset()).append(" ROWS");
        }

        if (limit.limit() != null) {
            sql.append(" FETCH FIRST ").append(limit.limit()).append(" ROWS ONLY");
        }
    }
}
