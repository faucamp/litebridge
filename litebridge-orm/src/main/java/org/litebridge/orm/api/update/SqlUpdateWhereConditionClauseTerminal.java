package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;

/**
 * The terminal interface for SQL update where condition clauses.
 */
public sealed interface SqlUpdateWhereConditionClauseTerminal

        extends UpdateWhereConditionClauseTerminal<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal,
        SqlQueryConditionBuilder<Row>>

        permits SqlUpdateWhereConditionClauseTerminalImpl {

}
