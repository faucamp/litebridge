package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public class MergeConditionClause<DTO, MUS extends MergeUpdateStep<DTO>>

        extends ConditionClauseImpl<DTO,
        MergeConditionClause<DTO, MUS>,
        MergeConditionClauseTerminal<DTO, MUS>> {

    public MergeConditionClause(final LitebridgeContext litebridgeContext,
                                final LogicOperator logicOperator,
                                final ExpressionSpec lhs,
                                final QueryNode node,
                                final Function<QueryNode, MergeConditionClauseTerminal<DTO, MUS>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, node, terminalRecreator);
    }
}
