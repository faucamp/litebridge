package org.litebridge.orm.api.delete;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public class SqlDeleteWhereConditionClause

        extends ConditionClauseImpl<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal>

        implements DeleteWhereConditionClause<Row,
        SqlDeleteWhereConditionClause,
        SqlDeleteWhereConditionClauseTerminal> {

    public SqlDeleteWhereConditionClause(final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final ExpressionSpec lhs,
                                         final Function<QueryNode, SqlDeleteWhereConditionClauseTerminal> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, null, terminalRecreator);
    }
}
