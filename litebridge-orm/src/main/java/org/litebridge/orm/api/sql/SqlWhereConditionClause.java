package org.litebridge.orm.api.sql;

import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.expression.ExpressionSpec;

public final class SqlWhereConditionClause
        extends ConditionClauseImpl<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal>

        implements WhereConditionClause<Row,
        SqlWhereConditionClause,
        SqlWhereConditionClauseTerminal,
        SqlGroupByClauseTerminal,
        SqlHavingConditionClause,
        SqlHavingConditionClauseTerminal,
        SqlOrderByClause,
        SqlOrderByClauseChain> {

    public SqlWhereConditionClause(final ConditionSpec conditionSpec,
                                   final LitebridgeContext litebridgeContext,
                                   final LogicOperator logicOperator,
                                   final ExpressionSpec lhs,
                                   final org.litebridge.orm.api.select.ast.QueryNode node,
                                   final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, SqlWhereConditionClauseTerminal> terminalRecreator) {
        super(conditionSpec, litebridgeContext, logicOperator, lhs, node, terminalRecreator);
    }
}
