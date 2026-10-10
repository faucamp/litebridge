package org.litebridge.orm.api.delete;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.condition.SqlQueryConditionBuilder;

/**
 * Terminal clause for SQL delete WHERE conditions.
 */
public sealed interface SqlDeleteWhereConditionClauseTerminal

        extends DeleteWhereConditionClauseTerminal<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal,
        SqlQueryConditionBuilder<Row>>

        permits SqlDeleteWhereConditionClauseTerminalImpl {

}
