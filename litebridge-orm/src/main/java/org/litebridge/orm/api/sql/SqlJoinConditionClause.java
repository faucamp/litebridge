package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlJoinConditionClause extends ConditionClauseImpl<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal>

        implements JoinConditionClause<Row,
        SqlJoinConditionClause,
        SqlJoinConditionClauseTerminal> {

    public SqlJoinConditionClause(final ConditionSpec conditionSpec,
                                  final LitebridgeContext litebridgeContext,
                                  final LogicOperator logicOperator,
                                  final ExpressionSpec lhs,
                                  final org.litebridge.orm.api.select.ast.QueryNode node,
                                  final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, SqlJoinConditionClauseTerminal> terminalRecreator) {
        super(conditionSpec, litebridgeContext, logicOperator, lhs, node, terminalRecreator);
    }
}
