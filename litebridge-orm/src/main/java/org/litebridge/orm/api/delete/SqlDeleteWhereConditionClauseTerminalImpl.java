package org.litebridge.orm.api.delete;

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
        return whereImpl(LogicOperator.AND, column);
    }

    @Override
    public SqlDeleteWhereConditionClause and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public SqlDeleteWhereConditionClauseTerminal and(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public SqlDeleteWhereConditionClause or(final String column) {
        return whereImpl(LogicOperator.OR, column);
    }

    @Override
    public SqlDeleteWhereConditionClause or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public SqlDeleteWhereConditionClauseTerminal or(final QueryConditionBuilder<Row> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    QueryNode node() {
        return node;
    }

    private SqlDeleteWhereConditionClause whereImpl(final LogicOperator logicOperator, final String column) {
        final Table table = litebridgeContext.tableRegistry().getOrCreateSpiTable(tableName);
        return whereImpl(logicOperator, new SelectColumnSpec(new Column(table, column)));
    }

    private SqlDeleteWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final Function<QueryNode, SqlDeleteWhereConditionClauseTerminal> recreator = n -> {
            this.node = new WhereNode(this.node, n);
            return this;
        };

        return new SqlDeleteWhereConditionClause(litebridgeContext, logicOperator, expression, recreator);
    }

    private SqlDeleteWhereConditionClauseTerminal whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final Table table = litebridgeContext.tableRegistry().getOrCreateSpiTable(tableName);
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(table, litebridgeContext.fromClauseEngine(), null);
        final AbstractCbConditionClauseTerminal<Row> terminal = query.apply(conditionClauseStart);
        this.node = new WhereNode(this.node, new ConditionGroupNode(null, logicOperator, terminal.node()));
        return this;
    }
}
