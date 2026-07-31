package org.litebridge.orm.api.sql.condition;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.select.ast.ConditionContext;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public class SqlConditionClauseStart extends AbstractConditionClauseStart<Row> {

    private final Table table;
    private final QueryNode node;

    public SqlConditionClauseStart(final Table table,
                                   final FromClauseEngine fromClauseEngine,
                                   final QueryNode node) {
        super(fromClauseEngine);
        this.table = table;
        this.node = node;
    }

    @Override
    public CbSqlConditionClause where(final String column) {
        final Column spiColumn = new Column(table, column);
        return (CbSqlConditionClause) where(new SelectColumnSpec(spiColumn));
    }


    @Override
    public AbstractCbConditionClause<Row> where(final ExpressionSpec expression) {
        return new CbSqlConditionClause(table, fromClauseEngine, LogicOperator.NOOP, expression, ConditionContext.WHERE, node, conditionNode -> new CbSqlConditionClauseTerminal(table, fromClauseEngine, conditionNode));
    }
}
