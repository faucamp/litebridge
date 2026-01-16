package org.litebridge.db.spi.query;

import org.litebridge.db.spi.Table;

import java.util.List;

/**
 * A database table join operation in a query.
 * <p>
 * This record encapsulates a target table and the conditions under which
 * the join operation is performed. A join operation combines rows from the
 * specified table with rows from another table, based on the provided conditions.
 * <p>
 * The target table is represented by the {@code Table} object, which may
 * include metadata such as catalog, schema, and name. The conditions are
 * represented as a list of {@code Condition} objects, each specifying a
 * column, operator, and optional value.
 * <p>
 * This record is used in query-building to specify join operations
 * within SQL select statements.
 *
 * @param table      The target table for the join operation.
 * @param conditions The list of conditions defining the join relationship.
 * @see Select
 */
public record Join(Table table, List<Condition> conditions) {
}
