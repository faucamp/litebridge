package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.function.BiFunction;

/**
 * SQL generator for UPDATE statements.
 */
public class UpdateSqlGenerator extends AbstractSqlGenerator {

    /**
     * Creates a new {@code UpdateSqlGenerator}.
     *
     * @param columnIdentifierGenerator the column identifier generator
     * @param mathOperationGenerator    the math operation generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public UpdateSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final MathOperationGenerator mathOperationGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData);
    }

    /**
     * Generates a SQL {@code UPDATE} statement string from the provided logical {@link Update} object.
     *
     * @param update             the {@link Update} object representing the logical update operation
     * @param connectionProvider the connection provider
     * @return the generated SQL statement string
     */
    public String generateSql(final Update update, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = new StringBuilder("UPDATE ");
        appendTable(sql, update.table());
        sql.append(" SET ");

        boolean first = true;

        for (UpdateColumn updateColumn : update.columns()) {
            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }

            sql.append(columnIdentifierGenerator.quoteIdentifier(updateColumn.name())).append(" = ");
            sql.append(getColumnValueFragment(updateColumn));
        }

        if (!update.where().isEmpty()) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, update.where(), update, connectionProvider);
        }

        return sql.toString();
    }
}
