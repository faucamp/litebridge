package org.litebridgedb.db.spi.query;

import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.SelectExpression;

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
 * @param orderBy     A list of ordering instructions specifying the order of the result set.
 * @param where       A list of conditions used to filter the data in the SELECT query.
 * @param limit       Optional pagination settings for limiting the number of rows in the result set.
 */
public record Select(Table table,
                     List<SelectExpression> expressions,
                     List<Join> joins,
                     List<Condition> where,
                     Optional<GroupBy> groupBy,
                     List<Condition> having,
                     List<OrderBy> orderBy,
                     Optional<Limit> limit) implements Operation {

}
