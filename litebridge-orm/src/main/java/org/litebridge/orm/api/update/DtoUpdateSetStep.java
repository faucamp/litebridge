package org.litebridge.orm.api.update;

import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * DTO-mode step for specifying the value of a column or expression in a SET clause.
 *
 * @param <DTO> the mapped DTO/entity type
 */
public final class DtoUpdateSetStep<DTO>

        extends UpdateSetStep<DTO,
        DtoUpdateStep<DTO>,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>> {

    /**
     * Creates a new {@code DtoUpdateSetStep} instance with a field name.
     *
     * @param field             the field name
     * @param node              the current query node
     * @param updateStepCreator the function to create the update step
     */
    public DtoUpdateSetStep(final String field, final QueryNode node, final Function<QueryNode, DtoUpdateStep<DTO>> updateStepCreator) {
        super(field, node, updateStepCreator);
    }

    /**
     * Creates a new {@code DtoUpdateSetStep} instance with an expression specification.
     *
     * @param expressionSpec    the expression specification
     * @param node              the current query node
     * @param updateStepCreator the function to create the update step
     */
    public DtoUpdateSetStep(final ExpressionSpec expressionSpec, final QueryNode node, final Function<QueryNode, DtoUpdateStep<DTO>> updateStepCreator) {
        super(expressionSpec, node, updateStepCreator);
    }
}
