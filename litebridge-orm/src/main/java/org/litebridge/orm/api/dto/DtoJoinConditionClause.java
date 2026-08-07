package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Represents a condition within a JOIN clause in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoJoinConditionClause<DTO> extends ConditionClauseImpl<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>>

        implements JoinConditionClause<DTO, DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    public DtoJoinConditionClause(final LitebridgeContext litebridgeContext,
                                  final LogicOperator logicOperator,
                                  final ExpressionSpec lhs,
                                  final QueryNode node,
                                  final Function<QueryNode, DtoJoinConditionClauseTerminal<DTO>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, node, terminalRecreator);
    }
}
