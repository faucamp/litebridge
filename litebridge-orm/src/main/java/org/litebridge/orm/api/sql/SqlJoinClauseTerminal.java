package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.JoinClauseTerminal;

import java.util.LinkedHashMap;

public interface SqlJoinClauseTerminal extends JoinClauseTerminal<LinkedHashMap<String, Object>,
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
