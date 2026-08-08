package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * DTO where condition clause.
 *
 * @param <DTO> the DTO type.
 */
public final class DtoWhereConditionClause<DTO>
        extends ConditionClauseImpl<DTO,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>>

        implements WhereConditionClause<DTO,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    public DtoWhereConditionClause(final LitebridgeContext litebridgeContext,
                                   final LogicOperator logicOperator,
                                   final ExpressionSpec lhs,
                                   final @Nullable QueryNode node,
                                   final Function<QueryNode, DtoWhereConditionClauseTerminal<DTO>> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhs, node, terminalRecreator);
    }
}
