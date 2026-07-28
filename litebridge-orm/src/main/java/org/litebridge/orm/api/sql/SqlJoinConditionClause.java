package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.ast.ConditionContext;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public final class SqlJoinConditionClause extends ConditionClauseImpl<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal>

        implements JoinConditionClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    public SqlJoinConditionClause(final LitebridgeContext litebridgeContext,
                                  final LogicOperator logicOperator,
                                  final ExpressionSpec lhs,
                                  final QueryNode node,
                                  final Function<QueryNode, SqlJoinConditionClauseTerminal> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, ConditionContext.JOIN, node, terminalRecreator);
    }
}
