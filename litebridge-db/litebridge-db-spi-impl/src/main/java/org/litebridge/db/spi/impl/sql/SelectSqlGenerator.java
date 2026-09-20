package org.litebridge.db.spi.impl.sql;

import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasedQuery;
import org.litebridge.db.spi.alias.AliasedTable;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.SelectTarget;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.util.function.BiFunction;

/**
 * SQL generator for SELECT statements.
 */
public class SelectSqlGenerator extends AbstractSqlGenerator {

    /**
     * Creates a new {@code SelectSqlGenerator}.
     *
     * @param columnIdentifierGenerator the column identifier generator
     * @param mathOperationGenerator    the math operation generator
     * @param ensureTableMetaData       a function to ensure table metadata
     */
    public SelectSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                              final MathOperationGenerator mathOperationGenerator,
                              final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData);
    }

    /**
     * Generates a SQL {@code SELECT} query string from the provided logical {@link Select} object.
     *
     * @param select             the {@link Select} object representing the logical select query
     * @param connectionProvider the connection provider
     * @return the generated SQL query string
     */
    public String generateSql(final Select select, final ConnectionProvider connectionProvider) {
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
            // Empty select clause; return all columns
            sql.append("*");
        }

        // From table
        sql.append(" FROM ");
        appendSelectTarget(sql, select.from(), connectionProvider);

        // Joins
        if (!CollectionUtils.isEmpty(select.joins())) {
            for (Join join : select.joins()) {
                sql.append(createJoin(join, select, connectionProvider));
            }
        }

        // Where
        if (select.where() != null) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, select.where(), select, connectionProvider);
        }

        // Group by
        if (!CollectionUtils.isEmpty(select.groupBy())) {
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

            if (select.having() != null) {
                sql.append(" HAVING ");
                appendConditionsAndSubgroups(sql, select.having(), select, connectionProvider);
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

        if (select.limit() != null) {
            appendLimitClause(select.limit(), sql);
        }

        return sql.toString();
    }

    protected void appendSelectTarget(final StringBuilder sql, final SelectTarget selectTarget, final ConnectionProvider connectionProvider) {
        switch (selectTarget) {
            case Table table -> appendTable(sql, table);
            case Select subselect -> sql.append('(')
                    .append(generateSql(subselect, connectionProvider))
                    .append(')');
            case AliasedQuery aliasedQuery -> sql.append('(')
                    .append(generateSql(aliasedQuery.target(), connectionProvider))
                    .append(") AS ")
                    .append(aliasedQuery.alias());
            case AliasedTable aliasedTable -> appendTable(sql, aliasedTable.target())
                    .append(" AS ")
                    .append(aliasedTable.alias());
        }
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
    protected String createJoin(final Join join, final Select operation, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = new StringBuilder(" JOIN ");
        appendSelectTarget(sql, join.target(), connectionProvider);

        if (join.conditions().conditions().size() == 1
                && join.conditions().subgroups().isEmpty()
                && join.conditions().conditions().getFirst().condition().operator() == Operator.USING) {
            sql.append(' ');
        } else {
            sql.append(" ON ");
        }

        appendConditionsAndSubgroups(sql, join.conditions(), operation, connectionProvider);
        return sql.toString();
    }

    /**
     * Appends a LIMIT clause to the SQL.
     *
     * @param limit the limit
     * @param sql   the SQL string builder
     */
    protected void appendLimitClause(final Limit limit, final StringBuilder sql) {
        if (limit.limit() != null) {
            sql.append(" LIMIT ").append(limit.limit());
        }

        if (limit.offset() != null) {
            sql.append(" OFFSET ").append(limit.offset());
        }
    }
}
