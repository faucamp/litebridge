package org.litebridge.db.sqlite.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.util.function.BiFunction;

/**
 * Specialised SQL generator for SELECT statements targeting SQLite databases.
 * <p>
 * This class extends the {@code SelectSqlGenerator} to provide SQLite-specific
 * SQL syntax for operations such as limiting and offsetting query results.
 */
public class SQLiteSelectSqlGenerator extends SelectSqlGenerator {

    /**
     * Creates a new {@code SQLiteSelectSqlGenerator}.
     *
     * @param columnIdentifierGenerator the column identifier generator
     * @param mathOperationGenerator    the math operation generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public SQLiteSelectSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                                    final MathOperationGenerator mathOperationGenerator,
                                    final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData);
    }

    @Override
    protected void appendLimitClause(final Limit limit, final StringBuilder sql) {
        if (limit.limit() != null) {
            sql.append(" LIMIT ").append(limit.limit());
        } else {
            sql.append(" LIMIT -1");
        }

        if (limit.offset() != null) {
            sql.append(" OFFSET ").append(limit.offset());
        }
    }
}
