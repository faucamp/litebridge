package org.litebridge.orm.api.sql.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public class CbSqlConditionClause extends AbstractCbConditionClause<Row> {

    private final String table;

    public CbSqlConditionClause(final String table,
                                final LitebridgeContext litebridgeContext,
                                final LogicOperator logicOperator,
                                final @Nullable String lhsColumn,
                                final @Nullable ExpressionSpec lhsExpression,
                                final @Nullable QueryNode node,
                                final Function<QueryNode, AbstractCbConditionClauseTerminal<Row>> terminalCreator) {
        super(litebridgeContext, logicOperator, lhsColumn, lhsExpression, node, terminalCreator);
        this.table = table;
    }

    @Override
    protected AbstractCbConditionClauseTerminal<Row> createCbConditionClauseTerminal(final QueryNode conditionNode) {
        return new CbSqlConditionClauseTerminal(table, conditionNode, litebridgeContext);
    }
}
