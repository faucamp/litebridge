package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.ValuesSpec;

/**
 * Terminal step for a MERGE ON condition clause.
 *
 * @param <DTO> the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <MUS> the merge update step type
 * @param <MIS> the merge insert step type
 */
public abstract sealed class MergeOnConditionClauseTerminal<DTO,
        SELF extends MergeOnConditionClauseTerminal<DTO, SELF, MUS, MIS, QCB>,
        MUS extends MergeUpdateStep,
        MIS extends MergeInsertStep,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        extends MergeWhenMatchedStep<DTO, MUS, MIS>

        implements ConditionClauseTerminal<DTO,
        MergeConditionClause<DTO, MUS, SELF, QCB>,
        SELF,
        QCB>

        permits DtoMergeOnConditionClauseTerminal, SqlMergeOnConditionClauseTerminal {

    protected final @Nullable String usingTable;
    protected final @Nullable Class<?> usingDtoClass;
    protected final @Nullable QueryNode usingQueryNode;
    protected final @Nullable ValuesSpec usingValues;
    private final @Nullable String usingAlias;
    private @Nullable UsingNode usingNode;

    /**
     * Creates a new {@code MergeOnConditionClauseTerminal} instance.
     *
     * @param usingTable        Table to use as the merge source.
     * @param usingDtoClass     DTO class to use as the merge source.
     * @param usingQueryNode    Subquery to merge on.
     * @param usingValues       Values to merge on.
     * @param usingAlias        Alias to use for the merge source.
     * @param on                The using on condition clause query node.
     * @param mergeNode         The root merge query node.
     * @param litebridgeContext Current Litebridge context.
     */
    public MergeOnConditionClauseTerminal(final @Nullable String usingTable,
                                          final @Nullable Class<?> usingDtoClass,
                                          final @Nullable QueryNode usingQueryNode,
                                          final @Nullable ValuesSpec usingValues,
                                          final @Nullable String usingAlias,
                                          final QueryNode on,
                                          final MergeNode mergeNode,
                                          final LitebridgeContext litebridgeContext) {
        super(mergeNode, on, litebridgeContext);
        this.usingTable = usingTable;
        this.usingDtoClass = usingDtoClass;
        this.usingQueryNode = usingQueryNode;
        this.usingValues = usingValues;
        this.usingAlias = usingAlias;
    }

    /**
     * Adds an AND condition with the specified column.
     *
     * @param column the column name
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, SELF, QCB> and(final String column) {
        return onImpl(LogicOperator.AND, column, null);
    }

    /**
     * Adds an AND condition with the specified expression.
     *
     * @param expression the expression specification
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, SELF, QCB> and(final ExpressionSpec expression) {
        return onImpl(LogicOperator.AND, null, expression);
    }

    /**
     * Adds an AND nested condition block.
     *
     * @param query the query condition builder
     * @return the condition clause terminal
     */
    @Override
    public SELF and(final QCB query) {
        return onImpl(LogicOperator.AND, query);
    }

    /**
     * Adds an OR condition with the specified column.
     *
     * @param column the column name
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, SELF, QCB> or(final String column) {
        return onImpl(LogicOperator.OR, column, null);
    }

    /**
     * Adds an OR condition with the specified expression.
     *
     * @param expression the expression specification
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, SELF, QCB> or(final ExpressionSpec expression) {
        return onImpl(LogicOperator.OR, null, expression);
    }

    /**
     * Adds an OR nested condition block.
     *
     * @param query the query condition builder
     * @return the condition clause terminal
     */
    @Override
    public SELF or(final QCB query) {
        return onImpl(LogicOperator.OR, query);
    }

    @Override
    QueryNode node() {
        if (usingNode == null) {
            usingNode = new UsingNode(mergeNode, usingTable, usingDtoClass, usingQueryNode, usingValues, usingAlias, node);
            return usingNode;
        } else {
            return node;
        }
    }

    @SuppressWarnings("unchecked")
    protected MergeConditionClause<DTO, MUS, SELF, QCB> onImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new MergeConditionClause<>(litebridgeContext,
                logicOperator,
                column,
                expression,
                node,
                conditionNode -> {
                    node = conditionNode;
                    return (SELF) this;
                });
    }

    protected abstract SELF onImpl(final LogicOperator logicOperator, final QCB query);
}
