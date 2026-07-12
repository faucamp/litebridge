package org.litebridge.orm.api.sql.delete;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.delete.DeleteWhereConditionClauseTerminal;

public sealed interface SqlDeleteWhereConditionClauseTerminal

        extends DeleteWhereConditionClauseTerminal<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal>

        permits SqlDeleteWhereConditionClauseTerminalImpl {

}
