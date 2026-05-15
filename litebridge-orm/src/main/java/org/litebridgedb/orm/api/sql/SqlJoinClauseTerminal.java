package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.JoinClauseTerminal;

public interface SqlJoinClauseTerminal extends JoinClauseTerminal<Row,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    SqlJoinClause join(final String table);
}
