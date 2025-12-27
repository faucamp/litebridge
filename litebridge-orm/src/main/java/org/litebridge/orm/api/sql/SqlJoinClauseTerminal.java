package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.JoinClauseTerminal;

public interface SqlJoinClauseTerminal extends JoinClauseTerminal<Row,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    SqlJoinClause join(final String schema, String table);

    default SqlJoinClause join(final String table) {
        return join("", table);
    }
}
