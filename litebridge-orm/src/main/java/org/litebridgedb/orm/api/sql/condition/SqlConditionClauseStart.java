package org.litebridgedb.orm.api.sql.condition;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.condition.AbstractCbConditionClause;
import org.litebridgedb.orm.api.condition.AbstractConditionClauseStart;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

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
