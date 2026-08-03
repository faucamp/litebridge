package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * SQL generator for UPDATE statements.
 */
public class UpdateSqlGenerator extends AbstractSqlGenerator {

    /**
     * Creates a new {@code UpdateSqlGenerator}.
     *
     * @param typeConverter             the type converter
     * @param columnIdentifierGenerator the column identifier generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public UpdateSqlGenerator(final TypeConverter typeConverter,
                              final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
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
        final StringBuilder sql = appendTable(new StringBuilder("UPDATE "), update.table())
                .append(" SET ");

        final List<org.litebridge.db.spi.sql.BindValue> bindValues = new ArrayList<>(update.columnValues().size());

        boolean first = true;

        for (ColumnValue columnValue : update.columnValues()) {
            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }

            sql.append(columnIdentifierGenerator.quoteIdentifier(columnValue.column().name())).append(" = ");
            final ColumnMetaData columnMetaData = ensureColumnMetaData(columnValue.column(), connectionProvider);

            if (columnValue.value() instanceof MathOperation mathOperation) {
                sql.append(createMathOperation(columnMetaData, mathOperation));
            } else {
                sql.append('?');
                final Object convertedValue = typeConverter.convert(columnValue.value(), columnMetaData.getDataType());
                bindValues.add(new org.litebridge.db.spi.sql.BindValue(convertedValue, columnMetaData.getDataType()));
            }
        }

        if (!update.where().isEmpty()) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, update.where(), update, connectionProvider);
        }

        return sql.toString();
    }

    /**
     * Creates a SQL representation of a math operation.
     *
     * @param column        the column
     * @param mathOperation the math operation
     * @return the SQL representation of the math operation
     */
    protected String createMathOperation(final ColumnMetaData column, final MathOperation mathOperation) {
        final Object convertedValue = typeConverter.convert(mathOperation.value(), column.getDataType());
        return "%s %s %s".formatted(columnIdentifierGenerator.quoteIdentifier(column.name()), mathOperation.operator().symbol(), convertedValue);
    }
}
