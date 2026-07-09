package org.litebridgedb.db.spi.impl.sql;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.math.MathOperation;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.update.ColumnValue;
import org.litebridgedb.db.spi.update.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class UpdateSqlGenerator extends AbstractSqlGenerator {

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
     * The resulting SQL query and its associated bind values are encapsulated in a {@link PreparedSql} object.
     *
     * @param update the {@link Update} object containing table metadata, column-value pairs for the SET clause,
     *               and conditions for the WHERE clause to specify target rows.
     * @return a {@link PreparedSql} object containing the generated SQL query string and the list of bind values.
     */
    public PreparedSql prepareSql(final Update update, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("UPDATE "), update.table())
                .append(" SET ");

        final List<org.litebridgedb.db.spi.sql.BindValue> bindValues = new ArrayList<>(update.columnValues().size());

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
                bindValues.add(new org.litebridgedb.db.spi.sql.BindValue(convertedValue, columnMetaData.getDataType()));
            }
        }

        if (!update.where().isEmpty()) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, update.where(), bindValues, update, connectionProvider);
        }

        return new PreparedSql(sql.toString(), bindValues);
    }

    protected String createMathOperation(final ColumnMetaData column, final MathOperation mathOperation) {
        final Object convertedValue = typeConverter.convert(mathOperation.value(), column.getDataType());
        return "%s %s %s".formatted(columnIdentifierGenerator.quoteIdentifier(column.name()), mathOperation.operator().symbol(), convertedValue);
    }
}
