package org.litebridge.orm.api.dto.condition;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractConditionClauseStart;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Start of a DTO-based condition clause.
 *
 * @param <DTO> the type of the DTO
 */
public class DtoConditionClauseStart<DTO> extends AbstractConditionClauseStart<DTO> {

    /**
     * Creates a new DTO condition clause start.
     *
     * @param node              the current query node
     * @param litebridgeContext the litebridge context
     */
    public DtoConditionClauseStart(final @Nullable QueryNode node,
                                   final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
    }

    @Override
    public CbDtoConditionClause<DTO> where(final String field) {
        return whereImpl(field, null);
    }

    @Override
    public AbstractCbConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(null, expression);
    }

    private @NonNull CbDtoConditionClause<DTO> whereImpl(final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new CbDtoConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                field,
                expression,
                node,
                node -> new CbDtoConditionClauseTerminal<>(node, litebridgeContext));
    }
}
