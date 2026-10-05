package org.litebridge.orm.api.select.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.JoinClauseTerminal;

/**
 * Terminal interface for SQL JOIN operations.
 */
public sealed interface SqlJoinClauseTerminal extends JoinClauseTerminal<Row,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain>

        permits SqlFromClauseTerminal, SqlJoinConditionClauseTerminal {

    /**
     * Adds a JOIN clause for the specified table name.
     *
     * @param table the table to join
     * @return the JOIN clause
     */
    SqlJoinClause join(final String table);
}
