package org.litebridge.orm.api.delete;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.update.UpdateStepBase;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

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
        final Column column = litebridgeContext.tableRegistry().getTableOrThrow(dtoClass)
                .getColumnForFieldName(field)
                .toColumn();
        return where(new SelectColumnSpec(column));
    }

    @Override
    public DtoDeleteWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return new DtoDeleteWhereConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                expression,
                node -> new DtoDeleteWhereConditionClauseTerminalImpl<>(dtoClass, new WhereNode(deleteNode, node), litebridgeContext));
    }

    QueryNode node() {
        return deleteNode;
    }
}
