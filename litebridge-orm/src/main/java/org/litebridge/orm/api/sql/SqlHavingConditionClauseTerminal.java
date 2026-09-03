package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlHavingConditionClauseTerminal
        extends AbstractHavingClauseTerminal<Row,
        SqlOrderByClause,
        SqlOrderByClauseChain>

        implements HavingConditionClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    private final String table;

    public SqlHavingConditionClauseTerminal(final String table,
                                            final QueryNode node,
                                            final SelectEngineTerminal selectEngineTerminal,
                                            final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
        this.table = table;
    }

    @Override
    public SqlHavingConditionClause and(final String column) {
        return havingImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlHavingConditionClause and(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlHavingConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return havingImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlHavingConditionClause or(final String column) {
        return havingImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlHavingConditionClause or(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public SqlHavingConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return havingImpl(LogicOperator.OR, query);
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... expressions) {
        return new SqlOrderByClause(expressions, node, selectEngineTerminal, litebridgeContext);
    }

    private SqlHavingConditionClause havingImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new SqlHavingConditionClause(litebridgeContext,
                LogicOperator.NOOP,
                column,
                expression,
                null,
                conditionNode -> new SqlHavingConditionClauseTerminal(table, new HavingNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlHavingConditionClauseTerminal havingImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(table, node, litebridgeContext);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();

        if (node instanceof HavingNode havingNode) {
            final ConditionGroupNode groupNode = new ConditionGroupNode(havingNode.condition(), logicOperator, conditionNode);
            havingNode.withCondition(groupNode);
            return this;
        }

        final ConditionGroupNode groupNode = new ConditionGroupNode(null, logicOperator, conditionNode);
        this.node = new HavingNode(node, groupNode);
        return this;
    }
}
