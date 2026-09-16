package org.litebridge.db.spi.query;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.SelectExpression;

import java.util.List;

/**
 * Logical representation of a SQL {@code SELECT} statement.
 * <p>
 * It is used by {@link org.litebridge.db.spi.DatabaseProvider} implementations
 * to generate SQL {@code SELECT} statement strings.
 *
 * @param table       The table from which data is being selected.
 * @param from        The aliased table/subquery from which data is being selected.
 * @param expressions A list of expressions (e.g. columns or functions) to be included in the SELECT query.
 * @param joins       A list of joins that define relationships with other tables.
 * @param where       The condition group used to filter the data in the SELECT query.
 * @param groupBy     A list of expressions used to group the result set.
 * @param having      The condition group used to filter the grouped data in the SELECT query.
 * @param orderBy     A list of ordering instructions specifying the order of the result set.
 * @param limit       Optional pagination settings for limiting the number of rows in the result set.
 */
public record Select(SelectTarget from,
                     List<SelectExpression> expressions,
                     @Nullable List<Join> joins,
                     @Nullable ConditionGroup where,
                     @Nullable List<SelectExpression> groupBy,
                     @Nullable ConditionGroup having,
                     @Nullable List<OrderBy> orderBy,
                     @Nullable Limit limit) implements Operation, SelectTarget {

    @Deprecated(forRemoval = true)
    public Select(final @Nullable Table table, final SelectTarget from, final List<SelectExpression> expressions, @Nullable final List<Join> joins, @Nullable final ConditionGroup where, @Nullable final List<SelectExpression> groupBy, @Nullable final ConditionGroup having, @Nullable final List<OrderBy> orderBy, @Nullable final Limit limit) {
        this(from, expressions, joins, where, groupBy, having, orderBy, limit);
    }

    @Override
    public SelectTarget table() {
        return from;
    }
}
