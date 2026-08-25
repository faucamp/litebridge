package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public final class SqlUpdateWhereConditionClause

        extends ConditionClauseImpl<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal>

        implements UpdateWhereConditionClause<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    public SqlUpdateWhereConditionClause(final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final ExpressionSpec lhs,
                                         final Function<QueryNode, SqlUpdateWhereConditionClauseTerminal> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, null, terminalRecreator);
    }
}
