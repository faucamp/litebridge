package org.litebridge.db.spi.impl.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * SQL generator for SELECT statements.
 */
public class SelectSqlGenerator extends AbstractSqlGenerator {

    /**
     * Creates a new {@code SelectSqlGenerator}.
     *
     * @param typeConverter             the type converter
     * @param columnIdentifierGenerator the column identifier generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public SelectSqlGenerator(final TypeConverter typeConverter,
                              final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
    }

    /**
     * Prepares a SQL SELECT statement along with its bind values for execution.
     *
     * @param select             the select operation
     * @param connectionProvider the connection provider
     * @return a {@link PreparedSql} object containing the generated SQL query string and the list of bind values
     */
    public PreparedSql prepareSql(final Select select, final ConnectionProvider connectionProvider) {
        return prepareSql(select, connectionProvider, null);
    }

    /**
     * Prepares a SQL SELECT statement along with its bind values for execution.
     *
     * @param select             the select operation
     * @param connectionProvider the connection provider
     * @param parentOperation    the parent operation, if any
     * @return a {@link PreparedSql} object containing the generated SQL query string and the list of bind values
     */
    public PreparedSql prepareSql(final Select select, final ConnectionProvider connectionProvider, final @Nullable Operation parentOperation) {
        final List<BindValue> bindValues = new ArrayList<>();
        final StringBuilder sql = new StringBuilder("SELECT ");

        boolean first = true;

        // Select expressions
        if (!CollectionUtils.isEmpty(select.expressions())) {
            for (final SelectExpression expression : select.expressions()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }

                final String identifier = expression.toSql(select, ClauseType.SELECT);
                sql.append(identifier);
            }
        } else {
            // Empty select clause; return all expressions
            sql.append("*");
        }

        // From table
        sql.append(" FROM ");
        appendTable(sql, select.table());

        if (select.table().alias() != null) {
            sql.append(' ').append(columnIdentifierGenerator.createAliasDeclaration(Objects.requireNonNull(select.table().alias())));
        }

        // Joins
        if (!CollectionUtils.isEmpty(select.joins())) {
            for (Join join : select.joins()) {
                final PreparedSql joinSql = createJoin(join, select, connectionProvider);
                sql.append(joinSql.sql());
                bindValues.addAll(joinSql.bindValues());
            }
        }

        // Where
        if (select.where().isPresent()) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, select.where().get(), bindValues, select, connectionProvider);
        }

        // Group by
        if (!select.groupBy().isEmpty()) {
            sql.append(" GROUP BY ");

            first = true;
            for (SelectExpression expression : select.groupBy()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }

                sql.append(expression.toSql(select, ClauseType.GROUP_BY));
            }

            if (select.having().isPresent()) {
                sql.append(" HAVING ");
                appendConditionsAndSubgroups(sql, select.having().get(), bindValues, select, connectionProvider);
            }
        }

        // Order by
        if (!CollectionUtils.isEmpty(select.orderBy())) {
            sql.append(" ORDER BY ");
            first = true;

            for (final OrderBy orderBy : select.orderBy()) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }

                final String identifier = orderBy.expression().toSql(select, ClauseType.ORDER_BY);
                sql.append(identifier).append(orderBy.asc() ? " ASC" : " DESC");
            }
        }

        select.limit().ifPresent(limit -> {
            appendLimitClause(limit, sql);
        });

        return new PreparedSql(sql.toString(), bindValues);
    }

    /**
     * Create a SQL JOIN clause based on the provided {@link Join} object.
     * <p>
     * The join clause is constructed by specifying the target table, optional schema,
     * and any associated conditions for the join operation. Conditional logic is applied
     * to determine the join type (e.g., ON or USING) and format the resulting SQL string.
     *
     * @param join               the {@link Join} object containing the target table information and the list
     *                           of conditions defining the join relationship
     * @param operation          the select operation
     * @param connectionProvider the connection provider
     * @return Prepared SQL join clause
     */
    protected PreparedSql createJoin(final Join join, final Select operation, final ConnectionProvider connectionProvider) {
        final StringBuilder sb = appendTable(new StringBuilder(" JOIN "), join.table());
        final List<@Nullable BindValue> bindValues = new ArrayList<>();

        if (join.table().alias() != null) {
            sb.append(' ').append(columnIdentifierGenerator.createAliasDeclaration(Objects.requireNonNull(join.table().alias())));
        }

        if (join.conditions().conditions().size() == 1
                && join.conditions().subgroups().isEmpty()
                && join.conditions().conditions().getFirst().condition().operator() == Operator.USING) {
            sb.append(' ');
        } else {
            sb.append(" ON ");
        }

        appendConditionsAndSubgroups(sb, join.conditions(), bindValues, operation, connectionProvider);
        return new PreparedSql(sb.toString(), bindValues);
    }

    /**
     * Appends a LIMIT clause to the SQL.
     *
     * @param limit the limit
     * @param sql   the SQL string builder
     */
    protected void appendLimitClause(final Limit limit, final StringBuilder sql) {
        limit.limit().ifPresent(limitVal -> sql.append(" LIMIT ").append(limitVal));
        limit.offset().ifPresent(offset -> sql.append(" OFFSET ").append(offset));
    }
}
