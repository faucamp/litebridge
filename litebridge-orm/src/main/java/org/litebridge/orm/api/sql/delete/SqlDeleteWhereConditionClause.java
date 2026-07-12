package org.litebridge.orm.api.sql.delete;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.delete.DeleteWhereConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;

public class SqlDeleteWhereConditionClause

        extends ConditionClauseImpl<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal>

        implements DeleteWhereConditionClause<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal> {

    public SqlDeleteWhereConditionClause(final ConditionSpec conditionSpec, final SqlDeleteWhereConditionClauseTerminal conditionTerminal, LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}
