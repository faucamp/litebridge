package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public final class SqlHavingConditionClause
        extends ConditionClauseImpl<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal>

        implements HavingConditionClause<Row,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlHavingConditionClause(final LitebridgeContext litebridgeContext,
                                    final LogicOperator logicOperator,
                                    final ExpressionSpec lhs,
                                    final QueryNode node,
                                    final Function<QueryNode, SqlHavingConditionClauseTerminal> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, node, terminalRecreator);
    }
}
