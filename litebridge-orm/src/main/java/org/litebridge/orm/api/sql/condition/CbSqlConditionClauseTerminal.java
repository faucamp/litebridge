package org.litebridge.orm.api.sql.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.QueryNode;
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
        return new CbSqlConditionClause(table, fromClauseEngine, logicOperator, expression, node, conditionNode -> new CbSqlConditionClauseTerminal(table, fromClauseEngine, conditionNode));
    }

    @Override
    protected AbstractCbConditionClauseTerminal<Row> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(table, fromClauseEngine, null);
        final ConditionClauseTerminal<Row, ?, ?> terminal = query.apply(conditionClauseStart);

        if (terminal instanceof AbstractCbConditionClauseTerminal<?> act) {
            return new CbSqlConditionClauseTerminal(table, fromClauseEngine, new ConditionGroupNode(node, logicOperator, act.node()));
        }

        return this;
    }
}
