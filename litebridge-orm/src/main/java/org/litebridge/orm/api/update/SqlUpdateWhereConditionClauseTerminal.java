package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Row;

/**
 * The terminal interface for SQL update where condition clauses.
 */
public sealed interface SqlUpdateWhereConditionClauseTerminal

        extends UpdateWhereConditionClauseTerminal<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal>

        permits SqlUpdateWhereConditionClauseTerminalImpl {

}
