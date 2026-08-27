package org.litebridge.db.spi.query;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.SelectExpression;

import java.util.List;
import java.util.Optional;

/**
 * SQL SELECT query structure.
 * <p>
 * This record encapsulates the components of a SELECT query, including the target table,
 * a list of expressions to retrieve, joins for combining data from other tables, conditions
 * for filtering data, ordering instructions, and optional pagination settings.
 *
 * @param table       The main table from which data is being selected.
 * @param expressions A list of expressions (e.g. columns or functions) to be included in the SELECT query.
 * @param joins       A list of joins that define relationships with other tables.
 * @param where       The condition group used to filter the data in the SELECT query.
 * @param groupBy     A list of expressions used to group the result set.
 * @param having      The condition group used to filter the grouped data in the SELECT query.
 * @param orderBy     A list of ordering instructions specifying the order of the result set.
 * @param limit       Optional pagination settings for limiting the number of rows in the result set.
 */
public record Select(Table table,
                     List<SelectExpression> expressions,
                     @Nullable List<Join> joins,
                     @Nullable ConditionGroup where,
                     @Nullable List<SelectExpression> groupBy,
                     @Nullable ConditionGroup having,
                     @Nullable List<OrderBy> orderBy,
                     @Nullable Limit limit) implements Operation {

}
