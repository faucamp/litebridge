package org.litebridge.db.spi.update;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;

import java.util.List;

/**
 * Logical representation of a SQL {@code UPDATE} statement.
 * <p>
 * It is used by {@link org.litebridge.db.spi.DatabaseProvider} implementations
 * to generate SQL {@code UPDATE} statement strings.
 *
 * @param table   The table being updated
 * @param columns list of columns to update
 * @param where   Defined filter criteria that determine which rows of the table will be updated.
 */
public record Update(Table table, List<UpdateColumn> columns, ConditionGroup where)
        implements UpdateStatement {
}
