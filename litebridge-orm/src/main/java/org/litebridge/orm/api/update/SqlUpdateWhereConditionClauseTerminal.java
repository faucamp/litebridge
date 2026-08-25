package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Row;

public sealed interface SqlUpdateWhereConditionClauseTerminal

        extends UpdateWhereConditionClauseTerminal<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal>

        permits SqlUpdateWhereConditionClauseTerminalImpl {

}
