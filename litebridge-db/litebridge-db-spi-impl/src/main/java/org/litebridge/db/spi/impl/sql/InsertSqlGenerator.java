package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.DatabaseProviderMetaData;
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

    private final DatabaseProviderMetaData.InsertCapability insertCapability;

    /**
     * Creates a new {@code InsertSqlGenerator}.
     *
     * @param columnIdentifierGenerator the column identifier generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public InsertSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final MathOperationGenerator mathOperationGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData,
                              final DatabaseProviderMetaData.InsertCapability insertCapability) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData);
        this.insertCapability = insertCapability;
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

        final int rows;

        if (insertCapability == DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS) {
            // Only insert a single row at a time (will be batched by the execution engine)
            rows = 1;
        } else {
            rows = insert.rows();
        }

        for (int i = 0; i < rows; i++) {
            if (i > 0) {
                sql.append(", ");
            }

            sql.append('(');

            for (int j = 0; j < insert.columns().size(); j++) {
                final UpdateColumn insertColumn = insert.columns().get(j);

                if (j > 0) {
                    sql.append(", ");
                }

                sql.append(getColumnValueFragment(insertColumn));
            }

            sql.append(')');
        }

        return sql.toString();
    }
}
