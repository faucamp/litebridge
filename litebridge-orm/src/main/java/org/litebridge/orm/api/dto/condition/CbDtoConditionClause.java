package org.litebridge.orm.api.dto.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClause;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Implementation of a condition clause for DTO-based queries.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public class CbDtoConditionClause<DTO> extends AbstractCbConditionClause<DTO> {

    private final Function<QueryNode, AbstractCbConditionClauseTerminal<DTO>> terminalCreator;

    /**
     * Constructs a new {@code CbDtoConditionClause}.
     *
     * @param logicOperator   The logical operator (AND/OR).
     * @param lhsExpression   The left-hand side expression.
     * @param node            The previous node in the chain.
     * @param terminalCreator The function to create the terminal clause.
     */
    public CbDtoConditionClause(final LitebridgeContext litebridgeContext,
                                final LogicOperator logicOperator,
                                final @Nullable String lhsColumn,
                                final @Nullable ExpressionSpec lhsExpression,
                                final @Nullable QueryNode node,
                                final Function<QueryNode, AbstractCbConditionClauseTerminal<DTO>> terminalCreator) {
        super(litebridgeContext, logicOperator, lhsColumn, lhsExpression, node, terminalCreator);
        this.terminalCreator = terminalCreator;
    }

    @Override
    protected AbstractCbConditionClauseTerminal<DTO> createCbConditionClauseTerminal(final QueryNode conditionNode) {
        return new CbDtoConditionClauseTerminal<>(conditionNode, litebridgeContext);
    }
}
