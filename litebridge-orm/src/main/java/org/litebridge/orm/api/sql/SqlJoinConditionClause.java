package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.api.select.model.ConditionSpec;

public final class SqlJoinConditionClause extends ConditionClauseImpl<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal>

        implements JoinConditionClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    public SqlJoinConditionClause(final ConditionSpec condition, final SqlJoinConditionClauseTerminal conditionTerminal) {
        super(condition, conditionTerminal);
    }
}
