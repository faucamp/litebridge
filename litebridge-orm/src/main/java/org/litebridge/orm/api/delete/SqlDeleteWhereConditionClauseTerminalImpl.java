package org.litebridge.orm.api.delete;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public final class SqlDeleteWhereConditionClauseTerminalImpl

        implements
        SqlDeleteWhereConditionClauseTerminal,
        DeleteTerminal {

    private final String tableName;
    private final LitebridgeContext litebridgeContext;
    private QueryNode node;

    public SqlDeleteWhereConditionClauseTerminalImpl(final String tableName,
                                                     final QueryNode node,
                                                     final LitebridgeContext litebridgeContext) {
        this.tableName = tableName;
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public SqlDeleteWhereConditionClause and(final String column) {
        return whereImpl(LogicOperator.AND, column, null);
    }

    @Override
    public SqlDeleteWhereConditionClause and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public SqlDeleteWhereConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlDeleteWhereConditionClause or(final String column) {
        return whereImpl(LogicOperator.OR, column, null);
    }

    @Override
    public SqlDeleteWhereConditionClause or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public SqlDeleteWhereConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    QueryNode node() {
        return node;
    }

    private SqlDeleteWhereConditionClause whereImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        final Function<QueryNode, SqlDeleteWhereConditionClauseTerminal> recreator = n -> {
            this.node = new WhereNode(this.node, n);
            return this;
        };

        return new SqlDeleteWhereConditionClause(litebridgeContext, logicOperator, column, expression, recreator);
    }

    private SqlDeleteWhereConditionClauseTerminal whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(tableName, node, litebridgeContext);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
        this.node = new WhereNode(this.node, new ConditionGroupNode(null, logicOperator, terminal.node()));
        return this;
    }
}
