package org.litebridgedb.db.spi.impl.sql;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.expression.SubselectExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.sql.BindValue;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;

import java.util.List;
import java.util.function.BiFunction;

public abstract class AbstractSqlGenerator {

    protected final TypeConverter typeConverter;
    protected final ColumnIdentifierGenerator columnIdentifierGenerator;
    private final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData;

    public AbstractSqlGenerator(final TypeConverter typeConverter,
                                final ColumnIdentifierGenerator columnIdentifierGenerator,
                                final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        this.typeConverter = typeConverter;
        this.columnIdentifierGenerator = columnIdentifierGenerator;
        this.ensureTableMetaData = ensureTableMetaData;
    }

    /**
     * Generate a SQL condition string based on the given {@link Condition}.
     * This method constructs the SQL fragment by combining the column, operator,
     * and value (if applicable) for the provided condition.
     *
     * @param condition the {@link Condition} object specifying the column, operator,
     *                  and value for the SQL condition
     * @return a {@code String} representing the constructed SQL condition fragment
     */
    protected PreparedSql createCondition(final Condition condition, final Operation operation, final ConnectionProvider connectionProvider) {
        final String column = columnIdentifierGenerator.createSelectColumnIdentifier(condition.column(), false, operation);
        final String sql;

        if (condition.operator() == Operator.IS_NULL || condition.operator() == Operator.IS_NOT_NULL) {
            sql = "%s %s".formatted(column, mapOperator(condition.operator()));
        } else if (condition.operator() == Operator.USING) {
            sql = "%s (%s)".formatted(mapOperator(condition.operator()), condition.column().name());
        } else {
            if (condition.value() instanceof SubselectExpression subselectExpression) {
                final PreparedSql subselectSql = subselectExpression.toSql(operation, connectionProvider);
                sql = "%s %s (%s)".formatted(column, mapOperator(condition.operator()), subselectSql.sql());
                return new PreparedSql(sql, subselectSql.bindValues());
            } else if (condition.value() instanceof SelectReference selectReference) {
                final Column referencedColumn = selectReference.column();
                sql = "%s %s %s.%s".formatted(column, mapOperator(condition.operator()), columnIdentifierGenerator.quoteIdentifier(referencedColumn.table().aliasOrName()), columnIdentifierGenerator.quoteIdentifier(referencedColumn.name()));
            } else {
                final ColumnMetaData columnMetaData = ensureTableMetaData(condition.column().table(), connectionProvider).column(condition.column().name());
                final Object rawValue = getExpressionValue(condition.value());
                final Object convertedValue = typeConverter.convert(rawValue, columnMetaData.getDataType());
                final BindValue bindValue = new BindValue(convertedValue, columnMetaData.getDataType());
                sql = "%s %s ?".formatted(column, mapOperator(condition.operator()));
                return new PreparedSql(sql, List.of(bindValue));
            }
        }

        return new PreparedSql(sql);
    }

    /**
     * Map an {@link Operator} enum to its corresponding string representation used in logical or database operations.
     *
     * @param operator the Operator enum to be mapped
     * @return the string representation of the provided Operator
     */
    protected String mapOperator(final Operator operator) {
        return switch (operator) {
            case EQ -> "=";
            case GT -> ">";
            case GTE -> ">=";
            case LT -> "<";
            case LTE -> "<=";
            case NEQ -> "<>";
            case IN -> "IN";
            case IS_NULL -> "IS NULL";
            case IS_NOT_NULL -> "IS NOT NULL";
            case USING -> "USING";
        };
    }

    protected StringBuilder appendTable(final StringBuilder sql, final Table table) {
        return appendTable(sql, table.schema(), table.name());
    }

    protected StringBuilder appendTable(final StringBuilder sql, final TableMetaData table) {
        return appendTable(sql, table.schema(), table.name());
    }

    protected StringBuilder appendTable(final StringBuilder sql, final String schema, final String table) {
        final ColumnIdentifierGenerator cig = columnIdentifierGenerator;

        if (!StringUtils.isBlank(schema)) {
            sql.append(cig.quoteIdentifier(schema)).append('.');
        }

        sql.append(cig.quoteIdentifier(table));
        return sql;
    }

    protected @Nullable Object getExpressionValue(final SelectExpression selectExpression) {
        if (selectExpression instanceof LiteralExpression literalExpression) {
            return literalExpression.value();
        } else {
            throw new UnsupportedOperationException("Unsupported select expression for value: " + selectExpression);
        }
    }

    protected TableMetaData ensureTableMetaData(final Table table, final ConnectionProvider connectionProvider) {
        return ensureTableMetaData.apply(table, connectionProvider);
    }

    protected ColumnMetaData ensureColumnMetaData(final Column column, final ConnectionProvider connectionProvider) {
        return ensureTableMetaData.apply(column.table(), connectionProvider).column(column.name());
    }
}
