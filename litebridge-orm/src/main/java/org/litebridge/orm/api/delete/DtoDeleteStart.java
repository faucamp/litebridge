package org.litebridge.orm.api.delete;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.update.UpdateStepBase;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * DTO-mode entry step for constructing a {@code DELETE} statement.
 *
 * @param <DTO> the mapped DTO/entity type
 */
public final class DtoDeleteStart<DTO> extends UpdateStepBase

        implements DeleteStart<DTO,
        DtoDeleteWhereConditionClause<DTO>,
        DtoDeleteWhereConditionClauseTerminal<DTO>>,
        DeleteTerminal {

    private final DeleteNode deleteNode;

    /**
     * Creates a new {@code DtoDeleteStart} instance.
     *
     * @param dtoClass          the mapped DTO/entity class to delete
     * @param litebridgeContext the Litebridge context
     */
    public DtoDeleteStart(final Class<DTO> dtoClass,
                          final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
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
                node -> new DtoDeleteWhereConditionClauseTerminalImpl<>(new WhereNode(deleteNode, node), litebridgeContext));
    }

    QueryNode node() {
        return deleteNode;
    }

    public DeleteTerminal withId(final Object id) {
        final WhereNode whereNode = new WhereNode(deleteNode, new ConditionWithIdNode(null, LogicOperator.NOOP, Operator.EQ, id));
        return new DtoDeleteWhereConditionClauseTerminalImpl<>(whereNode, litebridgeContext);
    }

    public DeleteTerminal withIds(final Iterable<?> ids) {
        ConditionWithIdNode conditionWithIdNode = null;

        for (Object id : ids) {
            conditionWithIdNode = new ConditionWithIdNode(conditionWithIdNode, LogicOperator.NOOP, Operator.EQ, id);
        }

        final WhereNode whereNode = new WhereNode(deleteNode, conditionWithIdNode);
        return new DtoDeleteWhereConditionClauseTerminalImpl<>(whereNode, litebridgeContext);
    }
}
