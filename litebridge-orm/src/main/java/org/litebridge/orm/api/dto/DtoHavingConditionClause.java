package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.HavingConditionClause;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Represents a HAVING condition clause for DTO queries.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoHavingConditionClause<DTO>
        extends ConditionClauseImpl<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>>

        implements HavingConditionClause<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    public DtoHavingConditionClause(final LitebridgeContext litebridgeContext,
                                    final LogicOperator logicOperator,
                                    final ExpressionSpec lhs,
                                    final @Nullable QueryNode node,
                                    final Function<QueryNode, DtoHavingConditionClauseTerminal<DTO>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, node, terminalRecreator);
    }
}
