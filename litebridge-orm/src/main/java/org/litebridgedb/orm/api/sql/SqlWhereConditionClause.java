package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.api.select.WhereConditionClause;
import org.litebridgedb.orm.api.select.impl.ConditionClauseImpl;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

public final class SqlWhereConditionClause
        extends ConditionClauseImpl<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal>

        implements WhereConditionClause<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlWhereConditionClause(final ConditionSpec conditionSpec, final SqlWhereConditionClauseTerminal conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(conditionSpec, conditionTerminal, litebridgeContext);
    }
}
