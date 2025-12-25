package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.JoinSpec;

import java.util.LinkedHashMap;

public class SqlJoinClause extends AbstractJoinClause<LinkedHashMap<String, Object>,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    public SqlJoinClause(final JoinSpec joinSpec, final AbstractSelector<LinkedHashMap<String, Object>> delegate) {
        super(joinSpec, delegate);
    }

    @Override
    public SqlJoinConditionClause on(final String column) {
        final SqlJoinConditionClauseTerminal joinConditionClauseTerminal = new SqlJoinConditionClauseTerminal(joinSpec, delegate);
        return new SqlJoinConditionClause(joinSpec.newCondition(column), joinConditionClauseTerminal);
    }
}
