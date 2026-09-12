package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Represents a condition clause in a MERGE statement.
 *
 * @param <DTO>  the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <MUS>  the merge update step type
 * @param <MCCT> the merge condition clause terminal type
 */
public class MergeConditionClause<DTO,
        MUS extends MergeUpdateStep,
        MCCT extends ConditionClauseTerminal<DTO, MergeConditionClause<DTO, MUS, MCCT>, MCCT>>

        extends ConditionClauseImpl<DTO, MergeConditionClause<DTO, MUS, MCCT>, MCCT> {

    /**
     * Creates a new {@code MergeConditionClause} instance.
     *
     * @param litebridgeContext the Litebridge context
     * @param logicOperator     the logical operator (AND/OR)
     * @param lhsColumn         the left-hand side column name
     * @param lhsExpression     the left-hand side expression
     * @param node              the previous query node in the chain
     * @param terminalRecreator the function to create the terminal clause
     */
    public MergeConditionClause(final LitebridgeContext litebridgeContext,
                                final LogicOperator logicOperator,
                                final @Nullable String lhsColumn,
                                final @Nullable ExpressionSpec lhsExpression,
                                final QueryNode node,
                                final Function<QueryNode, MCCT> terminalRecreator) {
        super(litebridgeContext, logicOperator, lhsColumn, lhsExpression, node, terminalRecreator);
    }
}
