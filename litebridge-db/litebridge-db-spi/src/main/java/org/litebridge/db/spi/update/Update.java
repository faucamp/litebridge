package org.litebridge.db.spi.update;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;

import java.util.List;

/**
 * A SQL UPDATE statement targeting a specific table, with associated columns and conditions.
 *
 * @param table   The table being updated
 * @param columns list of columns to update
 * @param where   Defined filter criteria that determine which rows of the table will be updated.
 */
public record Update(Table table, List<UpdateColumn> columns, ConditionGroup where)
        implements UpdateStatement {
}
