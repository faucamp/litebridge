package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.delete.DeleteWhereConditionClauseTerminal;

public sealed interface SqlDeleteWhereConditionClauseTerminal

        extends DeleteWhereConditionClauseTerminal<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal>

        permits SqlDeleteWhereConditionClauseTerminalImpl {

}
