package org.litebridge.orm.api.dto.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Implementation of a terminal condition clause for DTO-based queries.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public final class CbDtoConditionClauseTerminal<DTO> extends AbstractCbConditionClauseTerminal<DTO> {

    /**
     * Constructs a new {@code CbDtoConditionClauseTerminal}.
     *
     * @param node The current query node.
     */
    public CbDtoConditionClauseTerminal(final QueryNode node,
                                        final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
    }

    @Override
    protected CbDtoConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
        return whereImpl(logicOperator, field, null);
    }

    @Override
    protected CbDtoConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        return whereImpl(logicOperator, null, expression);
    }

    @Override
    protected AbstractCbConditionClauseTerminal<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        return new CbDtoConditionClauseTerminal<>(new ConditionGroupNode(node, logicOperator, terminal.node()), litebridgeContext);
    }

    private CbDtoConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new CbDtoConditionClause<>(litebridgeContext,
                logicOperator,
                field,
                expression,
                node,
                conditionNode -> new CbDtoConditionClauseTerminal<>(conditionNode, litebridgeContext));
    }
}
