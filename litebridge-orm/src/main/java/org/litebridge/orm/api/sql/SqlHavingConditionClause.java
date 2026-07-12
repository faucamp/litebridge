package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;

public final class SqlHavingConditionClause
        extends ConditionClauseImpl<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal>

        implements HavingConditionClause<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlHavingConditionClause(final ConditionSpec conditionSpec, final SqlHavingConditionClauseTerminal conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}
