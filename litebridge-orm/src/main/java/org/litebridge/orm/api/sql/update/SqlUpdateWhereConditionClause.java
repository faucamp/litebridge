package org.litebridge.orm.api.sql.update;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.update.UpdateWhereConditionClause;
import org.litebridge.orm.expression.ExpressionSpec;

public class SqlUpdateWhereConditionClause

        extends ConditionClauseImpl<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal>

        implements UpdateWhereConditionClause<Row,
        SqlUpdateWhereConditionClause,
        SqlUpdateWhereConditionClauseTerminal> {

    public SqlUpdateWhereConditionClause(final ConditionSpec conditionSpec,
                                         final LitebridgeContext litebridgeContext,
                                         final LogicOperator logicOperator,
                                         final ExpressionSpec lhs,
                                         final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, SqlUpdateWhereConditionClauseTerminal> terminalRecreator) {
        super(conditionSpec, litebridgeContext, logicOperator, lhs, null, terminalRecreator);
    }
}
