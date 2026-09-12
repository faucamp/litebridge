package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;

import java.util.function.BiFunction;

/**
 * Generator for SQL DELETE statements.
 */
public class DeleteSqlGenerator extends AbstractSqlGenerator {

    /**
     * Constructs a {@code DeleteSqlGenerator} with the specified components.
     *
     * @param columnIdentifierGenerator the generator for column identifiers
     * @param mathOperationGenerator    the math operation generator
     * @param ensureTableMetaData       the function to retrieve table metadata
     */
    public DeleteSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final MathOperationGenerator mathOperationGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData);
    }

    /**
     * Generates a SQL {@code DELETE} statement string from the provided logical {@link Delete} object.
     *
     * @param delete             the {@link Delete} object representing the logical delete operation
     * @param connectionProvider the connection provider
     * @return the generated SQL statement string
     */
    public String generateSql(final Delete delete, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("DELETE FROM "), delete.table());

        if (!delete.where().isEmpty()) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, delete.where(), delete, connectionProvider);
        }

        return sql.toString();
    }
}
