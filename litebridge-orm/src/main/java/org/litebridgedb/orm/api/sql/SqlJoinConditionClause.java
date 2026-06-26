package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.JoinConditionClause;
import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

public final class SqlJoinConditionClause extends ConditionClauseImpl<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal>

        implements JoinConditionClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    public SqlJoinConditionClause(final ConditionSpec condition, final SqlJoinConditionClauseTerminal conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(condition, conditionTerminal, litebridgeContext);
    }
}
