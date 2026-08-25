package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.util.function.Function;

public final class SqlUpdateWhereConditionClauseTerminalImpl implements SqlUpdateWhereConditionClauseTerminal {

    private final String tableName;
    private final LitebridgeContext litebridgeContext;
    private QueryNode node;

    public SqlUpdateWhereConditionClauseTerminalImpl(final String tableName,
                                                     final QueryNode node,
                                                     final LitebridgeContext litebridgeContext) {
        this.tableName = tableName;
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public SqlUpdateWhereConditionClause and(final String column) {
        return whereImpl(LogicOperator.AND, column);
    }

    @Override
    public SqlUpdateWhereConditionClause and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlUpdateWhereConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlUpdateWhereConditionClause or(final String column) {
        return whereImpl(LogicOperator.OR, column);
    }

    @Override
    public SqlUpdateWhereConditionClause or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public SqlUpdateWhereConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    private SqlUpdateWhereConditionClause whereImpl(final LogicOperator logicOperator, final String column) {
        final Table table = litebridgeContext.tableRegistry().getOrCreateSpiTable(tableName);
        return whereImpl(logicOperator, new SelectColumnSpec(new Column(table, column)));
    }

    private SqlUpdateWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final Function<QueryNode, SqlUpdateWhereConditionClauseTerminal> recreator = n -> {
            this.node = new WhereNode(this.node, n);
            return this;
        };

        return new SqlUpdateWhereConditionClause(litebridgeContext, logicOperator, expression, recreator);
    }

    private SqlUpdateWhereConditionClauseTerminalImpl whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final Table table = litebridgeContext.tableRegistry().getOrCreateSpiTable(tableName);
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(table, litebridgeContext.fromClauseEngine(), null);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
        this.node = new WhereNode(this.node, new ConditionGroupNode(null, logicOperator, terminal.node()));
        return this;
    }

    QueryNode node() {
        return node;
    }
}
