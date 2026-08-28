package org.litebridge.orm.api.sql.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public class SqlConditionClauseStart extends AbstractConditionClauseStart<Row> {

    private final String table;

    public SqlConditionClauseStart(final String table,
                                   final @Nullable QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        this.table = table;
    }

    @Override
    public CbSqlConditionClause where(final String column) {
        return new CbSqlConditionClause(table, litebridgeContext, LogicOperator.NOOP, column, null, node,
                conditionNode -> new CbSqlConditionClauseTerminal(table, conditionNode, litebridgeContext));
    }


    @Override
    public AbstractCbConditionClause<Row> where(final ExpressionSpec expression) {
        return new CbSqlConditionClause(table, litebridgeContext, LogicOperator.NOOP, null, expression, node,
                conditionNode -> new CbSqlConditionClauseTerminal(table, conditionNode, litebridgeContext));
    }
}
