package org.litebridge.orm.api.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlUpdateStep extends UpdateStepBase
        implements UpdateStep<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    private final String tableName;
    private QueryNode node;

    public SqlUpdateStep(final String tableName,
                         final QueryNode node,
                         final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.tableName = tableName;
        this.node = node;
    }

    @Override
    public SqlUpdateSetStep set(final String field) {
        return new SqlUpdateSetStep(field, node, node -> {
            this.node = node;
            return this;
        });
    }

    @Override
    public SqlUpdateSetStep set(final ExpressionSpec expression) {
        return new SqlUpdateSetStep(expression, node, node -> {
            this.node = node;
            return this;
        });
    }

    @Override
    public SqlUpdateWhereConditionClause where(final String column) {
        return whereImpl(column, null);
    }

    @Override
    public SqlUpdateWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(null, expression);
    }

    private SqlUpdateWhereConditionClause whereImpl(final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new SqlUpdateWhereConditionClause(litebridgeContext,
                LogicOperator.NOOP,
                column,
                expression,
                node -> new SqlUpdateWhereConditionClauseTerminalImpl(tableName, new WhereNode(this.node, node), litebridgeContext));
    }

    QueryNode node() {
        return node;
    }
}
