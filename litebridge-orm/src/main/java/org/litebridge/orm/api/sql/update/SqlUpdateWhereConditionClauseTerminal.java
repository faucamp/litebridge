package org.litebridge.orm.api.sql.update;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.update.UpdateWhereConditionClauseTerminal;

public sealed interface SqlUpdateWhereConditionClauseTerminal

        extends UpdateWhereConditionClauseTerminal<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal>

        permits SqlUpdateWhereConditionClauseTerminalImpl {

}
