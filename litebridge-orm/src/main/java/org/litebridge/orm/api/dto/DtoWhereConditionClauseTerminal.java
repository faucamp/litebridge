package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * DTO where condition clause terminal.
 *
 * @param <DTO> the DTO type.
 */
public final class DtoWhereConditionClauseTerminal<DTO>
        extends AbstractWhereClauseTerminal<DTO,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>

        implements WhereConditionClauseTerminal<DTO,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    /**
     * Constructs a new {@code DtoWhereConditionClauseTerminal}.
     */
    public DtoWhereConditionClauseTerminal(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoWhereConditionClause<DTO> and(final String field) {
        return whereImpl(LogicOperator.AND, field, null);
    }

    @Override
    public DtoWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public DtoWhereConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoWhereConditionClause<DTO> or(final String field) {
        return whereImpl(LogicOperator.OR, field, null);
    }

    @Override
    public DtoWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public DtoWhereConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.OR, query);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
//        return groupBy(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... fields) {
//        final QueryNode groupByNode = new GroupByNode(delegate.node(), fields);
//        return new DtoGroupByClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(groupByNode));
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
//        return orderBy(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
//        return new DtoOrderByClause<>(fields, (DtoSelector<DTO>) delegate);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        if (node instanceof WhereNode whereNode) {
            return new DtoWhereConditionClause<>(litebridgeContext,
                    logicOperator,
                    field,
                    expression,
                    whereNode.condition(),
                    conditionNode ->
                            new DtoWhereConditionClauseTerminal<>(whereNode.withCondition(conditionNode), selectEngineTerminal, litebridgeContext)
            );
        }

        return new DtoWhereConditionClause<>(litebridgeContext,
                logicOperator,
                field,
                expression,
                null,
                node -> new DtoWhereConditionClauseTerminal<>(new WhereNode(this.node, node), selectEngineTerminal, litebridgeContext));
    }

    private DtoWhereConditionClauseTerminal<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        if (!(node instanceof WhereNode whereNode)) {
            //TODO: remove scaffolding
            throw new IllegalArgumentException("AST error: Expected a WhereNode but got " + node);
        }

        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        whereNode.withCondition(new ConditionGroupNode(whereNode.condition(), logicOperator, terminal.node()));
        return this;
    }
}
