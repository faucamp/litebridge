package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.JoinClauseTerminal;

public interface SqlJoinClauseTerminal extends JoinClauseTerminal<Row,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    SqlJoinClause join(final String table);
}
