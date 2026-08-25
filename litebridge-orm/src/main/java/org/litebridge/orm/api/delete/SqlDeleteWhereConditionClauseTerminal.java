package org.litebridge.orm.api.delete;

import org.litebridge.db.spi.Row;

public sealed interface SqlDeleteWhereConditionClauseTerminal

        extends DeleteWhereConditionClauseTerminal<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal>

        permits SqlDeleteWhereConditionClauseTerminalImpl {

}
