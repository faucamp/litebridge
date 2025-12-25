package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;

import java.util.LinkedHashMap;

public class SqlFromClauseTerminal extends AbstractFromClauseTerminal<LinkedHashMap<String, Object>,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal> {

    public SqlFromClauseTerminal(final SqlSelector delegate) {
        super(delegate);
    }

    @Override
    public SqlJoinClause join(final String table) {
        return new SqlJoinClause(selectSpec.newJoinSpec(table), delegate);
    }

    @Override
    public SqlWhereConditionClause where(final String column) {
        return new SqlWhereConditionClause(selectSpec.newWhereCondition(column), new SqlWhereConditionClauseTerminal(delegate));
    }
}
