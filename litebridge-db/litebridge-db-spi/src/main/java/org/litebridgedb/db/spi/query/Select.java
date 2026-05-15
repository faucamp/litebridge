package org.litebridgedb.db.spi.query;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

import java.util.List;
import java.util.Optional;

/**
 * SQL SELECT query structure.
 * <p>
 * This record encapsulates the components of a SELECT query, including the target table,
 * a list of columns to retrieve, joins for combining data from other tables, conditions
 * for filtering data, ordering instructions, and optional pagination settings.
 *
 * @param table   The main table from which data is being selected.
 * @param columns A list of columns to be included in the SELECT query.
 * @param joins   A list of joins that define relationships with other tables.
 * @param orderBy A list of ordering instructions specifying the order of the result set.
 * @param where   A list of conditions used to filter the data in the SELECT query.
 * @param limit   Optional pagination settings for limiting the number of rows in the result set.
 */
public record Select(Table table,
                     List<Column> columns,
                     List<Join> joins,
                     List<OrderBy> orderBy,
                     List<Condition> where,
                     Optional<Limit> limit) {

}
