package org.litebridge.orm.api.sql.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public final class CbSqlConditionClauseTerminal extends AbstractCbConditionClauseTerminal<Row> {

    private final Table table;

    public CbSqlConditionClauseTerminal(final Table table, final FromClauseEngine fromClauseEngine, final QueryNode node) {
        super(fromClauseEngine, node);
        this.table = table;
    }

    @Override
    protected CbSqlConditionClause whereImpl(final LogicOperator logicOperator, final String column) {
        final Column spiColumn = new Column(table, column);
        return (CbSqlConditionClause) whereImpl(logicOperator, new SelectColumnSpec(spiColumn));
    }

    @Override
    protected CbSqlConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected AbstractCbConditionClause<Row> createCbConditionClause(final ConditionSpec conditionSpec) {
//        return new CbSqlConditionClause(table, fromClauseEngine);
        //TODO: reimplement
        throw new UnsupportedOperationException("Need to reimplement");
    }

    @Override
    protected AbstractConditionClauseStart<Row> createConditionClauseStart(final ConditionGroupSpec subgroup) {
//        return new SqlConditionClauseStart(subgroup, table, fromClauseEngine);
        //TODO: reimplement
        throw new UnsupportedOperationException("Need to reimplement");
    }

    @Override
    protected AbstractCbConditionClauseTerminal<Row> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
