package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlWhereConditionClauseTerminal
        extends AbstractWhereClauseTerminal<Row,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain>

        implements WhereConditionClauseTerminal<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlWhereConditionClauseTerminal(final QueryNode node,
                                           final SelectEngineTerminal selectEngineTerminal,
                                           final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlWhereConditionClause and(final String column) {
        return whereImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlWhereConditionClause and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlWhereConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlWhereConditionClause or(final String column) {
        return whereImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlWhereConditionClause or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public SqlWhereConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final String... columns) {
        return new SqlGroupByClauseTerminal(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final ExpressionSpec... expressions) {
        return new SqlGroupByClauseTerminal(expressions, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... expressions) {
        return new SqlOrderByClause(expressions, node, selectEngineTerminal, litebridgeContext);
    }

    private SqlWhereConditionClause whereImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        if (node instanceof WhereNode whereNode) {
            return new SqlWhereConditionClause(litebridgeContext,
                    logicOperator,
                    column,
                    expression,
                    whereNode.condition(),
                    node -> new SqlWhereConditionClauseTerminal(whereNode.withCondition(node), selectEngineTerminal, litebridgeContext));
        }

        return new SqlWhereConditionClause(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlWhereConditionClauseTerminal(new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private SqlWhereConditionClauseTerminal whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
//        if (!(delegate.node() instanceof WhereNode whereNode)) {
//            throw new IllegalArgumentException("AST error: Expected a WhereNode but got " + delegate.node());
//        }
//
//        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(((SqlSelector) delegate).table(), delegate.litebridgeContext().fromClauseEngine(), null);
//        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
//
//        whereNode.withCondition(new ConditionGroupNode(whereNode.condition(), logicOperator, terminal.node()));
//        return this;
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
