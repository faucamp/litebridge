package org.litebridgedb.orm.api.sql.delete;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.delete.DeleteWhereConditionClause;
import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

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
