package org.litebridge.orm.api.select.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents the terminal part of a JOIN condition clause in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoJoinConditionClauseTerminal<DTO>
        extends AbstractJoinConditionClauseTerminal<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>

        implements JoinClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>,

        DtoJoinClassTerminal<DTO> {

    /**
     * Creates a new instance of {@code DtoJoinConditionClauseTerminal}.
     *
     * @param joinNode             the join query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public DtoJoinConditionClauseTerminal(final JoinNode joinNode,
                                          final SelectEngineTerminal selectEngineTerminal,
                                          final LitebridgeContext litebridgeContext) {
        super(joinNode, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
        return joinImpl(LogicOperator.AND, field, null);
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public DtoJoinConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return joinImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoJoinConditionClause<DTO> or(final String field) {
        return joinImpl(LogicOperator.OR, field, null);
    }

    @Override
    public DtoJoinConditionClause<DTO> or(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public DtoJoinConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return joinImpl(LogicOperator.OR, query);
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final String field) {
        return whereImpl(LogicOperator.NOOP, field, null);
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, null, expression);
    }

    /**
     * Convenience method to select a DTO by its primary key.
     * <p>
     * Use a {@code Collection}/{@code List} to specify composite primary keys.
     *
     * @param id the primary key value
     * @return the selected DTO, if found
     */
    public Optional<DTO> withId(final Object id) {
        return createWithIdClause(id).one();
    }

    /**
     * Convenience method to select a DTO by its primary key.
     * <p>
     * Use a {@code Collection}/{@code List} to specify composite primary keys.
     *
     * @param id the primary key value
     * @return the selected DTO, or {@code null} if not found
     */
    public @Nullable DTO withIdOrNull(final Object id) {
        return createWithIdClause(id).oneOrNull();
    }

    /**
     * Retrieves a DTO by its primary key and throws an exception if no matching entry is found.
     * <p>
     * Use a {@code Collection}/{@code List} to specify composite primary keys.
     *
     * @param id the primary key value used to identify the DTO
     * @return the DTO associated with the given primary key
     * @throws NoSuchElementException if no DTO is found with the specified primary key
     */
    public DTO withIdOrThrow(final Object id) throws NoSuchElementException {
        return createWithIdClause(id).oneOrThrow();
    }

    /**
     * Retrieves a DTO by its primary key and throws the specified exception if no matching entry is found.
     * <p>
     * Use a {@code Collection}/{@code List} to specify composite primary keys.
     *
     * @param id                the primary key value used to identify the DTO
     * @param exceptionSupplier a supplier that provides the exception to be thrown if the DTO is not found
     * @param <X>               the type of exception to be thrown
     * @return the DTO associated with the given primary key
     * @throws X the exception provided by the supplier if no DTO is found with the specified primary key
     */
    public <X extends Throwable> DTO withIdOrThrow(final Object id, final Supplier<? extends X> exceptionSupplier) throws X {
        return createWithIdClause(id).oneOrThrow(exceptionSupplier);
    }

    @Override
    public DtoJoinClause<DTO> join(final Class<?> dtoClass) {
        return new DtoJoinClause<>(litebridgeContext, conditionNode -> {
            final JoinNode joinNode = new JoinNode(node, Join.JoinType.INNER, dtoClass, null, null, null, null);
            joinNode.setCondition(conditionNode);
            return new DtoJoinConditionClauseTerminal<>(joinNode, selectEngineTerminal, litebridgeContext);
        });
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
        return new DtoGroupByClauseTerminal<>(fields, node(), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... expressions) {
        return new DtoGroupByClauseTerminal<>(expressions, node(), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(fields, node(), selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, node(), selectEngineTerminal, litebridgeContext);
    }

    private DtoJoinConditionClause<DTO> joinImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new DtoJoinConditionClause<>(
                litebridgeContext,
                logicOperator,
                field,
                expression,
                node(),
                conditionNode -> {
                    joinNode.setCondition(conditionNode);
                    return this;
                });
    }

    private DtoJoinConditionClauseTerminal<DTO> joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = CbConditionClauseTerminalInspector.getNode(terminal);

        final ConditionGroupNode groupNode = new ConditionGroupNode(joinNode.condition(), logicOperator, conditionNode);
        joinNode.setCondition(groupNode);

        return this;
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new DtoWhereConditionClause<>(litebridgeContext,
                logicOperator,
                field,
                expression,
                null,
                conditionNode -> new DtoWhereConditionClauseTerminal<>(new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private DtoWhereConditionClauseTerminal<DTO> createWithIdClause(final Object id) {
        final WhereNode whereNode = new WhereNode(this.node, new ConditionWithIdNode(null, LogicOperator.NOOP, Operator.EQ, id));
        return new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);
    }
}
