package org.litebridge.orm.api.select.sql;

import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;
import org.litebridge.orm.api.select.JoinClauseTerminal;

/**
 * Terminal interface for SQL JOIN operations.
 */
public sealed interface SqlJoinClauseTerminal<ReturnType> extends JoinClauseTerminal<ReturnType,
        SqlJoinClause<ReturnType>,
        SqlJoinConditionClause<ReturnType>,
        SqlJoinConditionClauseTerminal<ReturnType>,
        SqlWhereConditionClause<ReturnType>,
        SqlWhereConditionClauseTerminal<ReturnType>,
        SqlGroupByClauseTerminal<ReturnType>,
        SqlHavingConditionClause<ReturnType>,
        SqlHavingConditionClauseTerminal<ReturnType>,
        SqlQueryConditionBuilder<ReturnType>,
        SqlOrderByClause<ReturnType>,
        SqlOrderByClauseChain<ReturnType>>

        permits SqlFromClauseTerminal, SqlJoinConditionClauseTerminal {

    /**
     * Adds a JOIN clause for the specified table name.
     *
     * @param table the table to join
     * @return the JOIN clause
     */
    SqlJoinClause<ReturnType> join(final String table);
}
