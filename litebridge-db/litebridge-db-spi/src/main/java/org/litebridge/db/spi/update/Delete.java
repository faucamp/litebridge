package org.litebridge.db.spi.update;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;

/**
 * Logical representation of a SQL {@code DELETE} statement.
 * <p>
 * It is used by {@link org.litebridge.db.spi.DatabaseProvider} implementations
 * to generate SQL {@code DELETE} statement strings.
 *
 * @param table The table from which rows will be deleted.
 * @param where The conditions for the rows to delete. If empty, all rows in the table
 *              will be deleted (depending on database permissions and constraints).
 */
public record Delete(Table table, ConditionGroup where)
        implements UpdateStatement {
}
