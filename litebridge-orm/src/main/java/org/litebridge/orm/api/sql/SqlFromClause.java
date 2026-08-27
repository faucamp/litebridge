package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.FromClause;

public final class SqlFromClause implements FromClause<Row,
        SqlFromClauseTerminal,
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

    private final SqlSelector delegate;

    public SqlFromClause(final SqlSelector delegate) {
        this.delegate = delegate;
    }

    @Override
    public SqlFromClauseTerminal from(final String table) {
//        return new SqlFromClauseTerminal(delegate);
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
