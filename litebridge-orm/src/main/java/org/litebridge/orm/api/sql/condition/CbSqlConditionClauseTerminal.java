package org.litebridge.orm.api.sql.condition;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class CbSqlConditionClauseTerminal extends AbstractCbConditionClauseTerminal<Row> {

    private final String table;

    public CbSqlConditionClauseTerminal(final String table,
                                        final QueryNode node,
                                        final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        this.table = table;
    }

    @Override
    protected CbSqlConditionClause whereImpl(final LogicOperator logicOperator, final String column) {
        return new CbSqlConditionClause(table, litebridgeContext, logicOperator, column, null, node,
                conditionNode -> new CbSqlConditionClauseTerminal(table, conditionNode, litebridgeContext));
    }

    @Override
    protected CbSqlConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        return new CbSqlConditionClause(table, litebridgeContext, logicOperator, null, expression, node,
                conditionNode -> new CbSqlConditionClauseTerminal(table, conditionNode, litebridgeContext));
    }

    @Override
    protected AbstractCbConditionClauseTerminal<Row> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(table, node, litebridgeContext);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
        return new CbSqlConditionClauseTerminal(table,
                new ConditionGroupNode(node, logicOperator, terminal.node()),
                litebridgeContext);
    }
}
