package org.litebridge.db.spi.update;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;

/**
 * A SQL DELETE statement targeting a specific table with optional conditions.
 * <p>
 * This class is a record that combines:
 * <ul>
 *     <li>A target {@link Table}, which specifies the table to delete rows from.</li>
 *     <li>A list of {@link Condition}, representing the WHERE clause of the DELETE statement.</li>
 * </ul>
 * <p>
 * Instances of this class are immutable and serve as part of the structure for building SQL
 * DELETE operations in a database.
 *
 * @param table The table from which rows will be deleted.
 * @param where The conditions for the rows to delete. If empty, all rows in the table
 *              will be deleted (depending on database permissions and constraints).
 */
public record Delete(Table table, ConditionGroup where)
        implements UpdateStatement {
}
