package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.HavingConditionClause;
import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

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
