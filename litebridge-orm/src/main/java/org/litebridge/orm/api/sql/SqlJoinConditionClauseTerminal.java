package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.JoinSpec;

import java.util.LinkedHashMap;

public class SqlJoinConditionClauseTerminal extends AbstractJoinConditionClauseTerminal<LinkedHashMap<String, Object>,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    public SqlJoinConditionClauseTerminal(final JoinSpec joinSpec, final AbstractSelector<LinkedHashMap<String, Object>> delegate) {
        super(joinSpec, delegate);
    }

    @Override
    public SqlJoinConditionClause and(final String column) {
        return new SqlJoinConditionClause(joinSpec.newCondition(column), this);
    }
}
