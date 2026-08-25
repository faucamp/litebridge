package org.litebridge.orm.api.update;

import org.litebridge.orm.api.select.ast.UpdateNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class DtoUpdateStart<DTO> extends UpdateStepBase

        implements UpdateStart<DTO,
        DtoUpdateStep<DTO>,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>> {

    private final Class<DTO> dtoClass;
    private final UpdateNode updateNode;

    public DtoUpdateStart(final Class<DTO> dtoClass,
                          final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.dtoClass = dtoClass;
        this.updateNode = new UpdateNode(null, null, dtoClass);
    }

    @Override
    public DtoUpdateSetStep<DTO> set(final String field) {
        return new DtoUpdateSetStep<>(field, updateNode, node -> new DtoUpdateStep<>(dtoClass, node, litebridgeContext));
    }

    @Override
    public DtoUpdateSetStep<DTO> set(final ExpressionSpec expression) {
        return new DtoUpdateSetStep<>(expression, updateNode, node -> new DtoUpdateStep<>(dtoClass, node, litebridgeContext));
    }
}
