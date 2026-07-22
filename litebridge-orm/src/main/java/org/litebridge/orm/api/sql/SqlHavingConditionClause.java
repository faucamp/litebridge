package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlHavingConditionClause
        extends ConditionClauseImpl<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal>

        implements HavingConditionClause<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlHavingConditionClause(final ConditionSpec conditionSpec,
                                   final LitebridgeContext litebridgeContext,
                                   final LogicOperator logicOperator,
                                   final ExpressionSpec lhs,
                                   final org.litebridge.orm.api.select.ast.QueryNode node,
                                   final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, SqlHavingConditionClauseTerminal> terminalRecreator) {
        super(conditionSpec, litebridgeContext, logicOperator, lhs, node, terminalRecreator);
    }
}
