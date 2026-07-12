package org.litebridge.orm.api.sql.update;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.update.UpdateWhereConditionClause;

public class SqlUpdateWhereConditionClause

        extends ConditionClauseImpl<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal>

        implements UpdateWhereConditionClause<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    public SqlUpdateWhereConditionClause(final ConditionSpec conditionSpec, final SqlUpdateWhereConditionClauseTerminal conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}
