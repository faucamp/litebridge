package org.litebridge.orm.api.sql.condition;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;

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
