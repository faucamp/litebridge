package org.litebridge.db.oracle.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.util.function.BiFunction;

/**
 * A specialised SQL generator for SELECT statements targeting Oracle databases.
 * <p>
 * This class extends the {@code SelectSqlGenerator} to provide Oracle-specific
 * SQL syntax for operations such as limiting and offsetting query results.
 * <p>
 * The primary distinction of this generator is its handling of the LIMIT clause
 * by translating it into Oracle-compatible pagination syntax using "OFFSET" and
 * "FETCH FIRST N ROWS ONLY".
 */
public class OracleSelectSqlGenerator extends SelectSqlGenerator {

    /**
     * Constructs a new {@code OracleSelectSqlGenerator}.
     *
     * @param columnIdentifierGenerator The generator for column identifiers.
     * @param ensureTableMetaData       A function to ensure table metadata.
     */
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
