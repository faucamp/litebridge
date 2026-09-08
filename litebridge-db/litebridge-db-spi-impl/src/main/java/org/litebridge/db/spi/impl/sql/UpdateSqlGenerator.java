package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.math.MathOperator;
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
     * Prepare a SQL UPDATE statement along with its bind values for execution.
     * <p>
     * This method constructs the SQL query string based on the provided {@link Update} object,
     * which contains the table's metadata, column-value pairs, and conditions for the WHERE clause.
     * It ensures proper formatting of the SQL query and converts values as needed using a type converter.
     *
     * @param update             the {@link Update} object containing table metadata, column-value pairs for the SET clause,
     *                           and conditions for the WHERE clause to specify target rows.
     * @param connectionProvider the connection provider
     * @return the generated SQL query string.
     */
    public String prepareSql(final Update update, final ConnectionProvider connectionProvider) {
        return prepareSql(update, false, connectionProvider);
    }

    public String prepareSql(final Update update, final boolean columnsOnly, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = new StringBuilder("UPDATE ");

        if (!columnsOnly) {
            appendTable(sql, update.table());
        }

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
