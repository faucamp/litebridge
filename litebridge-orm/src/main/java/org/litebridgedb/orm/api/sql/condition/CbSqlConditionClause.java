package org.litebridgedb.orm.api.sql.condition;

import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.condition.AbstractCbConditionClause;
import org.litebridgedb.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;

public class CbSqlConditionClause extends AbstractCbConditionClause<Row> {

    private final Table table;

    public CbSqlConditionClause(final ConditionSpec conditionSpec,
                                final ConditionGroupSpec conditionGroupSpec,
                                final Table table,
                                final FromClauseEngine fromClauseEngine) {
        super(conditionSpec, conditionGroupSpec, fromClauseEngine);
        this.table = table;
    }

    @Override
    protected AbstractCbConditionClauseTerminal<Row> createCbConditionClauseTerminal() {
        return new CbSqlConditionClauseTerminal(conditionGroupSpec, table, fromClauseEngine);
    }
}
