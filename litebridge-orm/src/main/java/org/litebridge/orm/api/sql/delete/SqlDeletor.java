package org.litebridge.orm.api.sql.delete;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.delete.impl.AbstractDeletor;
import org.litebridge.orm.api.delete.model.DeleteSpec;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.sql.condition.SqlConditionClauseStart;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.util.function.Function;

public final class SqlDeletor extends AbstractDeletor<DeleteSpec> implements SqlDeleteWhereClause {

    private final LitebridgeContext litebridgeContext;

    public SqlDeletor(final Table table,
                      final TransactionalDatabaseProvider databaseProvider,
                      final SelectExpressionMapper selectExpressionMapper,
                      final LitebridgeContext litebridgeContext) {
        super(new DeleteSpec(table, selectExpressionMapper), databaseProvider, litebridgeContext, new DeleteNode(null, table));
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public SqlDeleteWhereConditionClause where(final String column) {
        return whereImpl(LogicOperator.NOOP, column);
    }

    @Override
    public SqlDeleteWhereConditionClause where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
    }

    SqlDeleteWhereConditionClause whereImpl(final LogicOperator logicOperator, final String column) {
        return whereImpl(logicOperator, new SelectColumnSpec(new Column(deleteSpec.table(), column)));
    }

    SqlDeleteWhereConditionClause whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final Function<QueryNode, SqlDeleteWhereConditionClauseTerminal> recreator = n -> {
            this.node = new WhereNode(this.node, n);
            return new SqlDeleteWhereConditionClauseTerminalImpl(this);
        };
        return new SqlDeleteWhereConditionClause(litebridgeContext, logicOperator, expression, recreator);
    }

    SqlDeleteWhereConditionClauseTerminal whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<Row> query) {
        final SqlConditionClauseStart conditionClauseStart = new SqlConditionClauseStart(deleteSpec.table(), litebridgeContext.fromClauseEngine(), null);
        final ConditionClauseTerminal<Row, ?, ?> terminal = query.apply(conditionClauseStart);

        if (terminal instanceof AbstractCbConditionClauseTerminal<?> act) {
            this.node = new WhereNode(this.node, new ConditionGroupNode(null, logicOperator, act.node()));
        }

        return new SqlDeleteWhereConditionClauseTerminalImpl(this);
    }
}
