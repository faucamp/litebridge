package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlGroupByClauseTerminal extends AbstractGroupByClauseTerminal<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlGroupByClauseTerminal(final ExpressionSpec[] expressions,
                                    final QueryNode node,
                                    final SelectEngineTerminal selectEngineTerminal,
                                    final LitebridgeContext litebridgeContext) {
        super(expressions, new GroupByNode(node, null, expressions), selectEngineTerminal, litebridgeContext);
    }

    public SqlGroupByClauseTerminal(final String[] columns,
                                    final QueryNode node,
                                    final SelectEngineTerminal selectEngineTerminal,
                                    final LitebridgeContext litebridgeContext) {
        super(columns, new GroupByNode(node, columns, null), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlHavingConditionClause having(final ExpressionSpec expression) {
        return new SqlHavingConditionClause(litebridgeContext,
                LogicOperator.NOOP,
                null,
                expression,
                null,
                conditionNode -> new SqlHavingConditionClauseTerminal(new HavingNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    @Override
    public SqlOrderByClause orderBy(final String... columns) {
        return new SqlOrderByClause(columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlOrderByClause orderBy(final ExpressionSpec... expressions) {
        return new SqlOrderByClause(expressions, node, selectEngineTerminal, litebridgeContext);
    }
}
