package org.litebridge.orm.api.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.OrderByClauseTerminal;
import org.litebridge.orm.api.select.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.select.dto.DtoWhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;

/**
 * A builder that assists with programmatically constructe {@code WHERE} clauses.
 * <p>
 * While the main Litebridge API can be used, it can become cumbersome criteria-building conditional logic
 * due to the differences between {@code where()}, {@code and()}, and {@code or()}, and the various API model permutations.
 * <p>
 * This class allows queries to be built up conditionally by adding discrete conditions in a loop.
 */
public class DtoWhereCriteriaBuilder<DTO> {

    private final DtoFromClauseTerminal<?> select;
    private final LitebridgeContext litebridgeContext;
    private @Nullable QueryNode node;
    private @Nullable LogicOperator logicOperator;

    public DtoWhereCriteriaBuilder(final DtoFromClauseTerminal<DTO> select) {
        this.select = select;
        this.litebridgeContext = SelectTerminalInspector.getLitebridgeContext(select);
    }

    /**
     * Add a condition and group it with existing conditions using the current default logic operator.
     *
     * @param query Condition to add.
     * @return The criteria builder instance.
     * @see #setLogicOperator(LogicOperator) To set the current default logic operator.
     */
    public DtoWhereCriteriaBuilder<DTO> add(final DtoQueryConditionBuilder<DTO> query) {
        return createConditionNode(query, getLogicOperator());
    }

    /**
     * Add a condition and group it with existing conditions using the specified logic operator.
     *
     * @param query Condition to add.
     * @return The criteria builder instance.
     */
    public DtoWhereCriteriaBuilder<DTO> add(final LogicOperator logicOperator, final DtoQueryConditionBuilder<DTO> query) {
        return createConditionNode(query, logicOperator);
    }

    /**
     * Shorthand for {@code add(LogicOperator.AND, query)}
     *
     * @param query Condition to add.
     * @return The criteria builder instance.
     */
    public DtoWhereCriteriaBuilder<DTO> and(final DtoQueryConditionBuilder<DTO> query) {
        final LogicOperator logicOperator = node != null ? LogicOperator.AND : LogicOperator.NOOP;
        return createConditionNode(query, logicOperator);
    }

    /**
     * Shorthand for {@code add(LogicOperator.OR, query)}
     *
     * @param query Condition to add.
     * @return The criteria builder instance.
     */
    public DtoWhereCriteriaBuilder<DTO> or(final DtoQueryConditionBuilder<DTO> query) {
        final LogicOperator logicOperator = node != null ? LogicOperator.OR : LogicOperator.NOOP;
        return createConditionNode(query, logicOperator);
    }

    /**
     * Retrieves the current effective logical operator.
     * <p>
     * This is the operator that will be used when adding conditions with {@link #add(DtoQueryConditionBuilder)}
     * (when not specifying an operator).
     * <p>
     * Default: {@link LogicOperator#AND}
     *
     * @return The current default logical operator, or {@link LogicOperator#NOOP} if no condition exists yet.
     */
    public LogicOperator getLogicOperator() {
        if (node == null) {
            return LogicOperator.NOOP;
        }

        if (logicOperator == null) {
            logicOperator = LogicOperator.AND;
        }

        return logicOperator;
    }

    /**
     * Sets the current default logical operator.
     * <p>
     * This is the operator that will be used when adding conditions with {@link #add(DtoQueryConditionBuilder)}
     * (when not specifying an operator).
     * <p>
     * Default: {@link LogicOperator#AND}
     *
     * @param logicOperator The default logical operator to use going forward.
     */
    public void setLogicOperator(@Nullable final LogicOperator logicOperator) {
        this.logicOperator = logicOperator;
    }

    /**
     * Build the WHERE condition clause.
     *
     * @return new {@link DtoWhereConditionClauseTerminal} instance, or the original {@code select} if no conditions were added.
     */
    public OrderByClauseTerminal<DTO> build() {
        if (node == null) {
            return (OrderByClauseTerminal<DTO>) select;
        }

        final SelectNode selectNode = (SelectNode) SelectTerminalInspector.getNode(select);
        final SelectEngineTerminal selectEngineTerminal = SelectTerminalInspector.getSelectEngineTerminal(select);
        return new DtoWhereConditionClauseTerminal<>(new WhereNode(selectNode, node), selectEngineTerminal, litebridgeContext);
    }

    private DtoWhereCriteriaBuilder<DTO> createConditionNode(final DtoQueryConditionBuilder<DTO> query, final LogicOperator logicOperator) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(node, logicOperator, litebridgeContext);
        final CbDtoConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        node = CbConditionClauseTerminalInspector.getNode(terminal);
        return this;
    }
}
