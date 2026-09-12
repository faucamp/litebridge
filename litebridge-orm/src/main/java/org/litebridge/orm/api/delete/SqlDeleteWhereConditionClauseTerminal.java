package org.litebridge.orm.api.delete;

import org.litebridge.db.spi.Row;

/**
 * Terminal clause for SQL delete WHERE conditions.
 */
public sealed interface SqlDeleteWhereConditionClauseTerminal

        extends DeleteWhereConditionClauseTerminal<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal>

        permits SqlDeleteWhereConditionClauseTerminalImpl {

}
