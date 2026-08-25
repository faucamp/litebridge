package org.litebridge.orm.api.update;

import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public final class DtoUpdateSetStep<DTO>

        extends UpdateSetStep<DTO,
        DtoUpdateStep<DTO>,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>> {

    public DtoUpdateSetStep(final String field, final QueryNode node, final Function<QueryNode, DtoUpdateStep<DTO>> updateStepCreator) {
        super(field, node, updateStepCreator);
    }

    public DtoUpdateSetStep(final ExpressionSpec expressionSpec, final QueryNode node, final Function<QueryNode, DtoUpdateStep<DTO>> updateStepCreator) {
        super(expressionSpec, node, updateStepCreator);
    }
}
