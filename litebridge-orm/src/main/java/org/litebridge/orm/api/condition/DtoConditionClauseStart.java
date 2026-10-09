package org.litebridge.orm.api.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Start of a DTO-based condition clause.
 *
 * @param <DTO> the type of the DTO
 */
public class DtoConditionClauseStart<DTO> extends AbstractConditionClauseStart<DTO, CbDtoConditionClause<DTO>, CbDtoConditionClauseTerminal<DTO>, DtoQueryConditionBuilder<DTO>> {

    /**
     * Creates a new DTO condition clause start.
     *
     * @param node              the current query node
     * @param litebridgeContext the litebridge context
     */
    public DtoConditionClauseStart(final @Nullable QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(node, LogicOperator.NOOP, litebridgeContext);
    }

    /**
     * Creates a new DTO condition clause start.
     *
     * @param node              the current query node
     * @param logicOperator     the logic operator to group this query with the previous node
     * @param litebridgeContext the litebridge context
     */
    public DtoConditionClauseStart(final @Nullable QueryNode node,
                                   final LogicOperator logicOperator,
                                   final LitebridgeContext litebridgeContext) {
        super(node, logicOperator, litebridgeContext);
    }

    @Override
    public CbDtoConditionClause<DTO> where(final String field) {
        return whereImpl(field, null);
    }

    @Override
    public CbDtoConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(null, expression);
    }

    private CbDtoConditionClause<DTO> whereImpl(final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new CbDtoConditionClause<>(litebridgeContext,
                logicOperator,
                field,
                expression,
                node);
    }
}
