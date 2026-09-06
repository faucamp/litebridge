package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.function.BiFunction;

/**
 * SQL generator for INSERT statements.
 */
public class InsertSqlGenerator extends AbstractSqlGenerator {

    /**
     * Creates a new {@code InsertSqlGenerator}.
     *
     * @param columnIdentifierGenerator the column identifier generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public InsertSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, ensureTableMetaData);
    }

    /**
     * Prepare a SQL INSERT statement along with its bind values for execution.
     * <p>
     * This method constructs the SQL query string based on the provided {@link Insert} object,
     * which contains the table's metadata, expressions, and rows to be inserted.
     *
     * @param insert             the {@link Insert} object containing the table metadata, expressions, and rows for the SQL INSERT operation
     * @param connectionProvider the connection provider
     * @return the generated SQL query string
     */
    public String prepareSql(final Insert insert, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("INSERT INTO "), insert.table())
                .append(" (")
                .append(String.join(", ", insert.columns().stream()
                        .map(UpdateColumn::name)
                        .map(columnIdentifierGenerator::quoteIdentifier)
                        .toList()))
                .append(") VALUES ");

        for (int i = 0; i < insert.rows(); i++) {
            if (i > 0) {
                sql.append(", ");
            }

            sql.append('(');

            for (int j = 0; j < insert.columns().size(); j++) {
                final UpdateColumn insertColumn = insert.columns().get(j);

                if (j > 0) {
                    sql.append(", ");
                }

                if (insertColumn.generatedValue() != null) {
                    sql.append(insertColumn.generatedValue());
                } else {
                    sql.append('?');
                }
            }

            sql.append(')');
        }

        return sql.toString();
    }
}
