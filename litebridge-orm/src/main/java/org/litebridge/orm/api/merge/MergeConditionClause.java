package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public class MergeConditionClause<DTO,
        MUS extends MergeUpdateStep<DTO>,
        MCCT extends ConditionClauseTerminal<DTO, MergeConditionClause<DTO, MUS, MCCT>, MCCT>>

        extends ConditionClauseImpl<DTO, MergeConditionClause<DTO, MUS, MCCT>, MCCT> {

    public MergeConditionClause(final LitebridgeContext litebridgeContext,
                                final LogicOperator logicOperator,
                                final @Nullable String lhsColumn,
                                final @Nullable ExpressionSpec lhsExpression,
                                final QueryNode node,
                                final Function<QueryNode, MCCT> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsColumn, lhsExpression, node, terminalRecreator);
    }
}
