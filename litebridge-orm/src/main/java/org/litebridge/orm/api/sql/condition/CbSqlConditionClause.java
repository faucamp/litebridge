package org.litebridge.orm.api.sql.condition;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionContext;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public class CbSqlConditionClause extends AbstractCbConditionClause<Row> {

    private final Table table;

    public CbSqlConditionClause(final Table table,
                                final FromClauseEngine fromClauseEngine,
                                final LogicOperator logicOperator,
                                final ExpressionSpec lhs,
                                final ConditionContext conditionContext,
                                final QueryNode node,
                                final Function<QueryNode, AbstractCbConditionClauseTerminal<Row>> terminalCreator) {
        super(fromClauseEngine, logicOperator, lhs, conditionContext, node, terminalCreator);
        this.table = table;
    }

    @Override
    protected AbstractCbConditionClauseTerminal<Row> createCbConditionClauseTerminal(final QueryNode conditionNode) {
        return new CbSqlConditionClauseTerminal(table, fromClauseEngine, conditionNode);
    }
}
