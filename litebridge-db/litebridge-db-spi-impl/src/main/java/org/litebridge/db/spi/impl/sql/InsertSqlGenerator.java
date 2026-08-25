package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.db.spi.update.InsertV2;
import org.litebridge.db.spi.update.RowValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * SQL generator for INSERT statements.
 */
public class InsertSqlGenerator extends AbstractSqlGenerator {

    /**
     * Creates a new {@code InsertSqlGenerator}.
     *
     * @param typeConverter             the type converter
     * @param columnIdentifierGenerator the column identifier generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public InsertSqlGenerator(final TypeConverter typeConverter,
                              final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
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
        return prepareSql(insert, false, connectionProvider);
    }

    String prepareSql(final Insert insert, final boolean columnsOnly, final ConnectionProvider connectionProvider) {
        final List<String> columnNames = insert.columns().stream().map(Column::name).toList();
        final StringBuilder sql = new StringBuilder("INSERT");

        if (!columnsOnly) {
            appendTable(sql.append(" INTO "), insert.table());
        }

        sql.append(" (")
                .append(String.join(", ", columnNames.stream().map(columnIdentifierGenerator::quoteIdentifier).toList()))
                .append(") VALUES ");

        boolean first = true;

        for (RowValue row : insert.rows()) {
            if (!first) {
                sql.append(", ");
            }

            first = false;

            final PreparedRow preparedRow = prepareRow(row, connectionProvider);
            sql.append('(').append(String.join(", ", preparedRow.valueSpecifiers())).append(')');
        }

        return sql.toString();
    }

    public String prepareSql(final InsertV2 insert, final ConnectionProvider connectionProvider) {
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

    /**
     * Prepare a row for insertion based on the provided row value. This includes
     * processing column values, converting them to a suitable format, and generating
     * value specifiers and bind values for the prepared row. Handles nullable expressions,
     * auto-increment expressions, and sequence-based value generation as necessary.
     *
     * @param rowValue           the row value object containing the column definitions and their values
     * @param connectionProvider the connection provider
     * @return a PreparedRow instance containing processed value specifiers and bind values
     * @throws IllegalArgumentException if a non-nullable column without an auto-increment or sequence value is attempted to be set to NULL
     */
    protected PreparedRow prepareRow(final RowValue rowValue, final ConnectionProvider connectionProvider) {
        final List<String> valueSpecifiers = new ArrayList<>(rowValue.columns().size());
        final List<BindValue> bindValues = new ArrayList<>(rowValue.columns().size());

        for (final ColumnValue columnValue : rowValue.columns()) {
            final ColumnMetaData column = ensureColumnMetaData(columnValue.column(), connectionProvider);
            final Object convertedValue = typeConverter.convert(columnValue.value(), column.getDataType());

            if (convertedValue == null) {
                if (!column.isNullable() && !column.isAutoIncrement() && column.getGenerator() == null) {
                    throw new IllegalArgumentException("Attempting to insert NULL into non-nullable column: '%s'. Possible cause: column spec missing generator such as autoincrement/sequence".formatted(column.name()));
                } else if (column.getGenerator() != null) {
                    // Use the column value generator to add a value
                    valueSpecifiers.add(column.getGenerator().generate(column).toString());
                }
            } else {
                valueSpecifiers.add("?");
                bindValues.add(new BindValue(convertedValue, column.getDataType()));
            }
        }

        return new PreparedRow(valueSpecifiers, bindValues);
    }
}
