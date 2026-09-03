package org.litebridge.orm.api.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

public final class SqlFromClauseTerminal extends AbstractFromClauseTerminal<Row,
        SqlJoinClause,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain>

        implements SqlJoinClauseTerminal {

    private final String table;

    public SqlFromClauseTerminal(final SelectNode selectNode,
                                 final SelectEngineTerminal selectEngineTerminal,
                                 final LitebridgeContext litebridgeContext) {
        super(selectNode, selectEngineTerminal, litebridgeContext);
        this.table = Objects.requireNonNull(selectNode.table());
    }

    @Override
    public SqlJoinClause join(final String table) {
        return new SqlJoinClause(table, null, litebridgeContext, conditionNode -> {
            final JoinNode joinNode = new JoinNode(node, "INNER", null, table);
            joinNode.withCondition(conditionNode);
            return new SqlJoinConditionClauseTerminal(this.table, joinNode, selectEngineTerminal, litebridgeContext);
        });
    }

    @Override
    public SqlWhereConditionClause where(final String column) {
        return whereImpl(LogicOperator.NOOP, column, null);
    }

    @Override
    public SqlWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, null, expression);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final String... columns) {
        return new SqlGroupByClauseTerminal(table, columns, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public SqlGroupByClauseTerminal groupBy(final ExpressionSpec... expressions) {
        return new SqlGroupByClauseTerminal(table, expressions, node, selectEngineTerminal, litebridgeContext);
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
        return new SqlWhereConditionClause(litebridgeContext,
                logicOperator,
                column,
                expression,
                null,
                conditionNode -> new SqlWhereConditionClauseTerminal(table, new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }
}
