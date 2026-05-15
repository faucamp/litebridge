package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.update.UpdateWhereConditionClauseTerminal;

public sealed interface SqlUpdateWhereConditionClauseTerminal

        extends UpdateWhereConditionClauseTerminal<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal>

        permits SqlUpdateWhereConditionClauseTerminalImpl {

}
