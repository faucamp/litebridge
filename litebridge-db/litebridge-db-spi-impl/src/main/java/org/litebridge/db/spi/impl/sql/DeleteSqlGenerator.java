package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Delete;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Generator for SQL DELETE statements.
 */
public class DeleteSqlGenerator extends AbstractSqlGenerator {

    /**
     * Constructs a {@code DeleteSqlGenerator} with the specified components.
     *
     * @param typeConverter             the converter to use for SQL types
     * @param columnIdentifierGenerator the generator for column identifiers
     * @param ensureTableMetaData       the function to retrieve table metadata
     */
    public DeleteSqlGenerator(final TypeConverter typeConverter,
                              final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
    }

    /**
     * Prepares the SQL statement for a DELETE operation.
     *
     * @param delete             the delete operation metadata
     * @param connectionProvider the provider for database connections
     * @return the prepared SQL statement with bind values
     */
    public String prepareSql(final Delete delete, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("DELETE FROM "), delete.table());

        if (!delete.where().isEmpty()) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, delete.where(), delete, connectionProvider);
        }

        return sql.toString();
    }
}
