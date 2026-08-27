package org.litebridge.orm.api.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class DtoUpdateStep<DTO> extends UpdateStepBase
        implements UpdateStep<DTO,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>> {

    private final Class<DTO> dtoClass;
    private QueryNode node;

    public DtoUpdateStep(final Class<DTO> dtoClass,
                         final QueryNode node,
                         final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.dtoClass = dtoClass;
        this.node = node;
    }

    @Override
    public DtoUpdateSetStep<DTO> set(final String field) {
        return new DtoUpdateSetStep<>(field, node, node -> {
            this.node = node;
            return this;
        });
    }

    @Override
    public DtoUpdateSetStep<DTO> set(final ExpressionSpec expression) {
        return new DtoUpdateSetStep<>(expression, node, node -> {
            this.node = node;
            return this;
        });
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> where(final String field) {
        return whereImpl(field, null);
    }

    @Override
    public DtoUpdateWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(null, expression);
    }

    private DtoUpdateWhereConditionClause<DTO> whereImpl(final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new DtoUpdateWhereConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                field,
                expression,
                node -> new DtoUpdateWhereConditionClauseTerminalImpl<>(dtoClass, new WhereNode(this.node, node), litebridgeContext));
    }

    QueryNode node() {
        return node;
    }
}
