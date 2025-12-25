package org.litebridge.orm.api.sql;

import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.api.select.model.ConditionSpec;

import java.util.LinkedHashMap;

public class SqlJoinConditionClause extends ConditionClauseImpl<LinkedHashMap<String, Object>,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal>

        implements JoinConditionClause<LinkedHashMap<String, Object>,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    public SqlJoinConditionClause(final ConditionSpec condition, final SqlJoinConditionClauseTerminal conditionTerminal) {
        super(condition, conditionTerminal);
    }
}
