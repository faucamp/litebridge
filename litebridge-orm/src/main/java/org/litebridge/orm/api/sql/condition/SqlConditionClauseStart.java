package org.litebridge.orm.api.sql.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public class SqlConditionClauseStart extends AbstractConditionClauseStart<Row> {

    private final Table table;

    public SqlConditionClauseStart(final ConditionGroupSpec conditionGroupSpec,
                                   final Table table,
                                   final FromClauseEngine fromClauseEngine) {
        super(conditionGroupSpec, fromClauseEngine);
        this.table = table;
    }

    @Override
    public CbSqlConditionClause where(final String column) {
        final Column spiColumn = new Column(table, column);
        return (CbSqlConditionClause) where(new SelectColumnSpec(spiColumn));
    }

    @Override
    protected AbstractCbConditionClause<Row> createCbConditionClause(final ConditionSpec conditionSpec) {
        return new CbSqlConditionClause(conditionSpec, conditionGroupSpec, table, fromClauseEngine);
    }
}
