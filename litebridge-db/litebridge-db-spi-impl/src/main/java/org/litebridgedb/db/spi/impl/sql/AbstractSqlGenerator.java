package org.litebridgedb.db.spi.impl.sql;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.ConnectionProviderExpression;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.expression.SubselectExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.ConditionGroup;
import org.litebridgedb.db.spi.query.LogicCondition;
import org.litebridgedb.db.spi.query.LogicConditionGroup;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.sql.BindValue;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Abstract base class for SQL generators.
 */
public abstract class AbstractSqlGenerator {

    /**
     * The type converter used for mapping between database and Java types.
     */
    protected final TypeConverter typeConverter;

    /**
     * The generator for column identifiers.
     */
    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    /**
     * Function to ensure table metadata is available.
     */
    private final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData;

    /**
     * Constructs a new {@code AbstractSqlGenerator}.
     *
     * @param typeConverter             The type converter to use.
     * @param columnIdentifierGenerator The column identifier generator to use.
     * @param ensureTableMetaData       The function to ensure table metadata is available.
     */
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
     * @param condition          the {@link Condition} object specifying the column, operator,
     *                           and value for the SQL condition
     * @param operation          the current database operation
     * @param connectionProvider the connection provider
     * @return a {@link PreparedSql} representing the constructed SQL condition fragment
     */
    protected PreparedSql createCondition(final Condition condition, final Operation operation, final ConnectionProvider connectionProvider) {
        final String lhs = condition.lhs().toSql(operation, ClauseType.WHERE);
        final Column column;

        if (condition.lhs() instanceof ColumnExpression columnExpression) {
            column = columnExpression.column();
        } else {
            column = null;
        }

        final String sql;

        if (condition.operator() == Operator.IS_NULL || condition.operator() == Operator.IS_NOT_NULL) {
            sql = "%s %s".formatted(lhs, mapOperator(condition.operator()));
        } else if (condition.operator() == Operator.IN || condition.operator() == Operator.NOT_IN) {
            if (condition.rhs() instanceof LiteralExpression literalExpression) {
                sql = "%s %s (%s)".formatted(lhs, mapOperator(condition.operator()), literalExpression.toBindValueSql(operation));
                final Object rawValue = literalExpression.value();
                final List<BindValue> bindValues;

                if (rawValue == null) {
                    bindValues = List.of(createBindValue(column, null, connectionProvider));
                } else if (rawValue instanceof Collection<?> collection) {
                    bindValues = collection.stream()
                            .map(item -> createBindValue(column, item, connectionProvider))
                            .toList();
                } else {
                    bindValues = List.of(createBindValue(column, rawValue, connectionProvider));
                }

                return new PreparedSql(sql, bindValues);
            } else {
                final String sqlFragment;
                final List<@Nullable BindValue> bindValues;

                if (condition.rhs() instanceof ConnectionProviderExpression connectionProviderExpression) {
                    PreparedSql fragmentPreparedSql = connectionProviderExpression.toSql(operation, connectionProvider);
                    sqlFragment = fragmentPreparedSql.sql();
                    bindValues = fragmentPreparedSql.bindValues();
                } else {
                    sqlFragment = Objects.requireNonNull(condition.rhs()).toSql(operation, ClauseType.WHERE);
                    bindValues = Collections.emptyList();
                }

                sql = "%s %s (%s)".formatted(lhs, mapOperator(condition.operator()), sqlFragment);
                return new PreparedSql(sql, bindValues);
            }
        } else if (condition.operator() == Operator.USING) {
            sql = "%s (%s)".formatted(mapOperator(condition.operator()),
                    ObjectUtils.requireNonNull(column, () -> new IllegalArgumentException("JOIN USING clause without column target"))
                            .name());
        } else {
            if (condition.rhs() instanceof SubselectExpression subselectExpression) {
                final PreparedSql subselectSql = subselectExpression.toSql(operation, connectionProvider);
                sql = "%s %s (%s)".formatted(lhs, mapOperator(condition.operator()), subselectSql.sql());
                return new PreparedSql(sql, subselectSql.bindValues());
            } else if (condition.rhs() instanceof SelectReference selectReference) {
                final Column referencedColumn = selectReference.column();
                sql = "%s %s %s.%s".formatted(lhs, mapOperator(condition.operator()), columnIdentifierGenerator.quoteIdentifier(referencedColumn.table().aliasOrName()), columnIdentifierGenerator.quoteIdentifier(referencedColumn.name()));
            } else {
                final Object rawValue = getExpressionValue(Objects.requireNonNull(condition.rhs()));
                final BindValue bindValue = createBindValue(column, rawValue, connectionProvider);
                sql = "%s %s ?".formatted(lhs, mapOperator(condition.operator()));
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
            case LIKE -> "LIKE";
            case IN -> "IN";
            case NOT_IN -> "NOT IN";
            case IS_NULL -> "IS NULL";
            case IS_NOT_NULL -> "IS NOT NULL";
            case USING -> "USING";
        };
    }

    /**
     * Appends a table name to the SQL builder, quoting identifiers.
     *
     * @param sql   The SQL builder.
     * @param table The table to append.
     * @return The SQL builder.
     */
    protected StringBuilder appendTable(final StringBuilder sql, final Table table) {
        return appendTable(sql, table.schema(), table.name());
    }

    /**
     * Appends a table name to the SQL builder, quoting identifiers.
     *
     * @param sql    The SQL builder.
     * @param schema The schema name.
     * @param table  The table name.
     * @return The SQL builder.
     */
    protected StringBuilder appendTable(final StringBuilder sql, @Nullable final String schema, final String table) {
        final ColumnIdentifierGenerator cig = columnIdentifierGenerator;

        if (!StringUtils.isBlank(schema)) {
            sql.append(cig.quoteIdentifier(schema)).append('.');
        }

        sql.append(cig.quoteIdentifier(table));
        return sql;
    }

    /**
     * Extracts the value from a select expression.
     *
     * @param selectExpression The select expression.
     * @return The extracted value.
     */
    protected @Nullable Object getExpressionValue(final SelectExpression selectExpression) {
        if (selectExpression instanceof LiteralExpression literalExpression) {
            return literalExpression.value();
        } else {
            throw new UnsupportedOperationException("Unsupported select expression for RHS: " + selectExpression);
        }
    }

    /**
     * Ensures that table metadata is available for the specified table.
     *
     * @param table              The table.
     * @param connectionProvider The connection provider.
     * @return The table metadata.
     */
    protected TableMetaData ensureTableMetaData(final Table table, final ConnectionProvider connectionProvider) {
        return ensureTableMetaData.apply(table, connectionProvider);
    }

    /**
     * Ensures that column metadata is available for the specified column.
     *
     * @param column             The column.
     * @param connectionProvider The connection provider.
     * @return The column metadata.
     */
    protected ColumnMetaData ensureColumnMetaData(final Column column, final ConnectionProvider connectionProvider) {
        return ensureTableMetaData.apply(column.table(), connectionProvider).column(column.name());
    }

    /**
     * Creates a bind value for a column and raw value.
     *
     * @param column             The column.
     * @param rawValue           The raw value.
     * @param connectionProvider The connection provider.
     * @return The bind value.
     */
    protected BindValue createBindValue(final @Nullable Column column, final @Nullable Object rawValue, final ConnectionProvider connectionProvider) {
        final BindValue bindValue;

        if (column != null) {
            final ColumnMetaData columnMetaData = ensureTableMetaData(column.table(), connectionProvider).column(column.name());
            final Object convertedValue = typeConverter.convert(rawValue, columnMetaData.getDataType());
            bindValue = new BindValue(convertedValue, columnMetaData.getDataType());
        } else if (rawValue != null) {
            bindValue = new BindValue(rawValue, typeConverter.getSqlDataType(rawValue.getClass()));
        } else {
            bindValue = new BindValue(null, Types.NULL);
        }

        return bindValue;
    }

    /**
     * Appends conditions and subgroups to the SQL builder.
     *
     * @param sql                The SQL builder.
     * @param conditionGroup     The condition group.
     * @param bindValues         The list of bind values to populate.
     * @param operation          The current database operation.
     * @param connectionProvider The connection provider.
     */
    protected void appendConditionsAndSubgroups(final StringBuilder sql,
                                                final ConditionGroup conditionGroup,
                                                final List<@Nullable BindValue> bindValues,
                                                final Operation operation,
                                                final ConnectionProvider connectionProvider) {

        for (LogicCondition logicCondition : conditionGroup.conditions()) {
            if (logicCondition.logicOperator() != LogicOperator.NOOP) {
                sql.append(' ').append(logicCondition.logicOperator()).append(' ');
            }

            final PreparedSql conditionSql = createCondition(logicCondition.condition(), operation, connectionProvider);
            sql.append(conditionSql.sql());
            bindValues.addAll(conditionSql.bindValues());
        }

        for (LogicConditionGroup logicConditionGroup : conditionGroup.subgroups()) {
            if (logicConditionGroup.logicOperator() != LogicOperator.NOOP) {
                sql.append(' ').append(logicConditionGroup.logicOperator());
            }

            sql.append(" (");
            appendConditionsAndSubgroups(sql, logicConditionGroup.conditionGroup(), bindValues, operation, connectionProvider);
            sql.append(')');
        }
    }
}
