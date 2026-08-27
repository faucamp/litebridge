package org.litebridge.orm.api.delete;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.update.UpdateStepBase;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class DtoDeleteStart<DTO> extends UpdateStepBase

        implements DeleteStart<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>>,
        DeleteTerminal {

    private final Class<DTO> dtoClass;
    private final DeleteNode deleteNode;

    public DtoDeleteStart(final Class<DTO> dtoClass,
                          final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.dtoClass = dtoClass;
        this.deleteNode = new DeleteNode(null, null, dtoClass);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final String field) {
        return whereImpl(field, null);
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(null, expression);
    }

    private DtoDeleteWhereConditionClause<DTO> whereImpl(final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new DtoDeleteWhereConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                field,
                expression,
                node -> new DtoDeleteWhereConditionClauseTerminalImpl<>(dtoClass, new WhereNode(deleteNode, node), litebridgeContext));
    }

    QueryNode node() {
        return deleteNode;
    }
}
