package org.litebridge.orm.api.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.condition.DtoQueryConditionBuilder;
import org.litebridge.orm.api.select.SelectApiImpl;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.ExistsExpressionSpec;

/**
 * DTO-mode step for specifying SET assignments or WHERE conditions in an {@code UPDATE} statement.
 *
 * @param <DTO> the mapped DTO/entity type
 */
public final class DtoUpdateStep<DTO> extends UpdateStepBase
        implements UpdateStep<DTO,
        DtoUpdateStep<DTO>,
        DtoUpdateSetStep<DTO>,
        DtoUpdateWhereConditionClause<DTO>,
        DtoUpdateWhereConditionClauseTerminal<DTO>,
        DtoQueryConditionBuilder<DTO>> {

    private QueryNode node;

    /**
     * Creates a new {@code DtoUpdateStep} instance.
     *
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public DtoUpdateStep(final QueryNode node,
                         final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
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

    @Override
    public DtoUpdateWhereConditionClauseTerminal<DTO> where(final ExistsExpressionSpec existsExpression) {
        final SelectTerminal<?> selectTerminal = existsExpression.query().apply(new SelectApiImpl(litebridgeContext));
        final QueryNode subselectNode = SelectTerminalInspector.getNode(selectTerminal);
        final QueryNode conditionNode = new ConditionNode(null, LogicOperator.NOOP, null, null, Operator.EXISTS, subselectNode);
        node = new WhereNode(this.node, conditionNode);
        return new DtoUpdateWhereConditionClauseTerminalImpl<>(node, litebridgeContext);
    }

    private DtoUpdateWhereConditionClause<DTO> whereImpl(final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new DtoUpdateWhereConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                field,
                expression,
                node -> new DtoUpdateWhereConditionClauseTerminalImpl<>(new WhereNode(this.node, node), litebridgeContext));
    }

    QueryNode node() {
        return node;
    }
}
