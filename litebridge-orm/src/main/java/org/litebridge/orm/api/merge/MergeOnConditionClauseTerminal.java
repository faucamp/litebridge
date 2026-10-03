package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.CbConditionClauseTerminalInspector;
import org.litebridge.orm.api.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
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
public final class MergeOnConditionClauseTerminal<DTO,
        MUS extends MergeUpdateStep,
        MIS extends MergeInsertStep>

        extends MergeWhenMatchedStep<DTO, MUS, MIS>

        implements ConditionClauseTerminal<DTO,
        MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>>,
        MergeOnConditionClauseTerminal<DTO, MUS, MIS>> {

    private final @Nullable String usingTable;
    private final @Nullable Class<?> usingDtoClass;
    private final @Nullable QueryNode usingQueryNode;
    private final @Nullable ValuesSpec usingValues;
    private final @Nullable String usingAlias;
    private @Nullable UsingNode usingNode;

    /**
     * Creates a new {@code MergeOnConditionClauseTerminal} instance.
     *
     * @param mergeNode         the root merge query node
     * @param on                the using on condition clause query node
     * @param litebridgeContext the Litebridge context
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
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> and(final String column) {
        return onImpl(LogicOperator.AND, column, null);
    }

    /**
     * Adds an AND condition with the specified expression.
     *
     * @param expression the expression specification
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> and(final ExpressionSpec expression) {
        return onImpl(LogicOperator.AND, null, expression);
    }

    /**
     * Adds an AND nested condition block.
     *
     * @param query the query condition builder
     * @return the condition clause terminal
     */
    @Override
    public MergeOnConditionClauseTerminal<DTO, MUS, MIS> and(final QueryConditionBuilder<DTO> query) {
        return onImpl(LogicOperator.AND, query);
    }

    /**
     * Adds an OR condition with the specified column.
     *
     * @param column the column name
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> or(final String column) {
        return onImpl(LogicOperator.OR, column, null);
    }

    /**
     * Adds an OR condition with the specified expression.
     *
     * @param expression the expression specification
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> or(final ExpressionSpec expression) {
        return onImpl(LogicOperator.OR, null, expression);
    }

    /**
     * Adds an OR nested condition block.
     *
     * @param query the query condition builder
     * @return the condition clause terminal
     */
    @Override
    public MergeOnConditionClauseTerminal<DTO, MUS, MIS> or(final QueryConditionBuilder<DTO> query) {
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

    private MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> onImpl(final LogicOperator logicOperator, final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new MergeConditionClause<>(litebridgeContext,
                logicOperator,
                column,
                expression,
                node,
                conditionNode -> {
                    node = conditionNode;
                    return this;
                });
    }

    private MergeOnConditionClauseTerminal<DTO, MUS, MIS> onImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        node = new ConditionGroupNode(node, logicOperator, CbConditionClauseTerminalInspector.getNode(terminal));
        return this;
    }
}
