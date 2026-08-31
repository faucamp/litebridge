package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Terminal clause for DTO FROM clauses.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoFromClauseTerminal<DTO> extends AbstractFromClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>

        implements DtoJoinClassTerminal<DTO> {

    /**
     * Creates a new DtoFromClauseTerminal.
     */
    public DtoFromClauseTerminal(final SelectNode selectNode,
                                 final SelectEngineTerminal selectEngineTerminal,
                                 final LitebridgeContext litebridgeContext) {
        super(selectNode, selectEngineTerminal, litebridgeContext);
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
     * Adds a nested condition clause.
     * <p>
     * The nested condition clause is grouped with parentheses to ensure proper SQL syntax.
     *
     * @param query Function that builds the nested condition clause
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    public DtoWhereConditionClauseTerminal<DTO> where(final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        return new DtoWhereConditionClauseTerminal<>(new WhereNode(this.node, terminal.node()), selectEngineTerminal, litebridgeContext);
    }

    /**
     * Convenience method to select a DTO by its primary key.
     *
     * @param id the primary key value
     * @return the selected DTO, if found
     */
    public Optional<DTO> withId(final Object id) {
        return createWithIdClause(id).one();
    }

    /**
     * Convenience method to select a DTO by its primary key.
     *
     * @param id the primary key value
     * @return the selected DTO, or {@code null} if not found
     */
    public @Nullable DTO withIdOrNull(final Object id) {
        return createWithIdClause(id).oneOrNull();
    }

    /**
     * Retrieves a DTO by its primary key and throws an exception if no matching entry is found.
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
//        final OrmTable joinTable;
//
//        // First check for inline/contextually-registered tables
//        final OrmTable contextScopedTable = ormTable.getContextTableRegistry().getOrmTable(dtoClass);
//
//        if (contextScopedTable != null) {
//            joinTable = contextScopedTable;
//        } else {
//            joinTable = tableRegistry.getTableOrThrow(dtoClass);
//        }
//
//
//        return new DtoJoinClause<>((DtoSelector<DTO>) delegate, joinTable, node -> {
//            final JoinNode joinNode = new JoinNode(delegate.node(), "INNER", joinTable.dtoClass(), ormTable.dtoClass(), null);
//            joinNode.withCondition(node);
//            delegate.withNode(joinNode);
//            return new DtoJoinConditionClauseTerminal<>(joinNode, (DtoSelector<DTO>) delegate);
//        });
        return new DtoJoinClause<>(null, litebridgeContext, conditionNode -> {
            final JoinNode joinNode = new JoinNode(node, "INNER", dtoClass, null);
            joinNode.withCondition(conditionNode);
            return new DtoJoinConditionClauseTerminal(joinNode, selectEngineTerminal, litebridgeContext);
        });
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
        return new DtoGroupByClauseTerminal(fields, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... expressions) {
        return new DtoGroupByClauseTerminal(expressions, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(fields, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, node, selectEngineTerminal, litebridgeContext);
    }

    private DtoWhereConditionClauseTerminal<DTO> createWithIdClause(final Object id) {
        final WhereNode whereNode = new WhereNode(this.node, new ConditionWithIdNode(null, LogicOperator.NOOP, Operator.EQ, id));
        return new DtoWhereConditionClauseTerminal<>(whereNode, selectEngineTerminal, litebridgeContext);
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new DtoWhereConditionClause<>(litebridgeContext,
                logicOperator,
                field,
                expression,
                null,
                conditionNode -> new DtoWhereConditionClauseTerminal<>(new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }
}
