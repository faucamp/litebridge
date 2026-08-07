package org.litebridge.orm.api.condition;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.ConditionClause;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.impl.DelegatingSelector;
import org.litebridge.orm.api.select.impl.DelegatingSelectorInspector;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Abstract base class for condition clauses in the fluent select API.
 *
 * @param <DTO> The type of the DTO being queried.
 */
public abstract class AbstractCbConditionClause<DTO> implements ConditionClause<DTO, AbstractCbConditionClause<DTO>, AbstractCbConditionClauseTerminal<DTO>> {

    /**
     * The engine used to process the FROM clause.
     */
    protected final FromClauseEngine fromClauseEngine;
    private final LogicOperator logicOperator;
    private final ExpressionSpec lhs;
    private final QueryNode node;
    private final Function<QueryNode, AbstractCbConditionClauseTerminal<DTO>> terminalCreator;

    /**
     * Constructs a new {@code AbstractCbConditionClause}.
     *
     * @param fromClauseEngine The FROM clause engine.
     * @param logicOperator    The logic operator (AND/OR).
     * @param lhs              The left-hand side expression.
     * @param node             The previous node in the chain.
     * @param terminalCreator  The function to create the terminal clause.
     */
    public AbstractCbConditionClause(final FromClauseEngine fromClauseEngine,
                                     final LogicOperator logicOperator,
                                     final ExpressionSpec lhs,
                                     final QueryNode node,
                                     final Function<QueryNode, AbstractCbConditionClauseTerminal<DTO>> terminalCreator) {
        this.logicOperator = logicOperator;
        this.lhs = lhs;
        this.fromClauseEngine = fromClauseEngine;
        this.node = node;
        this.terminalCreator = terminalCreator;
    }

    /**
     * Equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> eq(final @Nullable Object value) {
        return condition(Operator.EQ, value);
    }

    /**
     * Equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> eq(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.EQ, subselect, true);
    }

    /**
     * Not equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> neq(final @Nullable Object value) {
        return condition(Operator.NEQ, value);
    }

    /**
     * Not equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> neq(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.NEQ, subselect, true);
    }

    /**
     * Less than
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> lt(final Object value) {
        return condition(Operator.LT, Objects.requireNonNull(value, "Operator LT requires a non-NULL RHS value"));
    }

    /**
     * Less than
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> lt(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.LT, subselect, false);
    }

    /**
     * Less than or equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> lte(final Object value) {
        return condition(Operator.LTE, value);
    }

    /**
     * Less than or equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> lte(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.LTE, subselect, false);
    }

    /**
     * Greater than
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> gt(final Object value) {
        return condition(Operator.GT, value);
    }

    /**
     * Greater than
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> gt(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.GT, subselect, false);
    }

    /**
     * Greater than or equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> gte(final Object value) {
        return condition(Operator.GTE, value);
    }

    /**
     * Greater than or equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> gte(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.GTE, subselect, false);
    }

    /**
     * Like
     *
     * @param value The operand for the like expression.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    @Override
    public AbstractCbConditionClauseTerminal<DTO> like(final String value) {
        return condition(Operator.LIKE, value);
    }

    @Override
    public AbstractCbConditionClauseTerminal<DTO> in(final Object value, final Object... otherValues) {
        if (value instanceof Collection<?> collection && otherValues.length == 0) {
            return in(collection);
        }

        return in(Stream.concat(Stream.of(value), Arrays.stream(otherValues)).toList());
    }

    @Override
    public AbstractCbConditionClauseTerminal<DTO> in(final Collection<?> values) {
        return condition(Operator.IN, values);
    }

    @Override
    public AbstractCbConditionClauseTerminal<DTO> in(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.IN, subselect, false);
    }

    @Override
    public AbstractCbConditionClauseTerminal<DTO> notIn(final Object value, final Object... otherValues) {
        if (value instanceof Collection<?> collection && otherValues.length == 0) {
            return notIn(collection);
        }

        return notIn(Stream.concat(Stream.of(value), Arrays.stream(otherValues)).toList());
    }

    @Override
    public AbstractCbConditionClauseTerminal<DTO> notIn(final Collection<?> values) {
        return condition(Operator.NOT_IN, values);
    }

    @Override
    public AbstractCbConditionClauseTerminal<DTO> notIn(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.NOT_IN, subselect, false);
    }

    /**
     * Null comparison.
     * <p>
     * Equivalent to {@code eq(null)}.
     *
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> isNull() {
        return condition(Operator.IS_NULL, null);
    }

    /**
     * Not null comparison.
     * <p>
     * Equivalent to {@code neq(null)}.
     *
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public AbstractCbConditionClauseTerminal<DTO> isNotNull() {
        return condition(Operator.IS_NOT_NULL, null);
    }

    private AbstractCbConditionClauseTerminal<DTO> subselectImpl(final Operator operator,
                                                                 final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect,
                                                                 final boolean allowNull) {
        // To support the current overloading and null parameters
        if (subselect == null) {
            if (allowNull) {
                return condition(operator, null);
            }

            throw new NullPointerException("Operator " + operator + " requires a non-NULL RHS value");
        }

        return condition(operator, createSelectSpec(subselect));
    }

    /**
     * Creates a condition terminal for the specified operator and value.
     *
     * @param operator The operator for the condition.
     * @param value    The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    private AbstractCbConditionClauseTerminal<DTO> condition(final Operator operator, @Nullable final Object value) {
        final Operator translatedOperator;

        if (value == null) {
            translatedOperator = switch (operator) {
                case EQ -> Operator.IS_NULL;
                case NEQ -> Operator.IS_NOT_NULL;
                case IS_NULL, IS_NOT_NULL -> operator;
                default ->
                        throw new IllegalArgumentException("Operator %s does not support null value".formatted(operator));
            };
        } else {
            translatedOperator = operator;
        }

        final QueryNode conditionNode = new ConditionNode(node, logicOperator, lhs, translatedOperator, value);

        return createCbConditionClauseTerminal(conditionNode);
    }

    /**
     * Creates a new terminal condition clause instance.
     *
     * @param conditionNode the condition node to wrap in a terminal clause
     * @return A new {@link AbstractCbConditionClauseTerminal} instance.
     */
    protected abstract AbstractCbConditionClauseTerminal<DTO> createCbConditionClauseTerminal(final QueryNode conditionNode);

    private SelectSpec createSelectSpec(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        final SelectTerminal<?> selectTerminal = Objects.requireNonNull(subselect, "Subselect cannot be null")
                .apply(new SelectEngine(fromClauseEngine));
        return getSelectSpec(selectTerminal);
    }

    private SelectSpec getSelectSpec(final SelectTerminal<?> selectTerminal) {
        final AbstractSelector<?, ?> selector = switch (selectTerminal) {
            case DelegatingSelector<?, ?> delegating -> DelegatingSelectorInspector.getDelegate(delegating);
            case AbstractSelector<?, ?> s -> s;
            default ->
                    throw new IllegalArgumentException("Unsupported terminal type: " + selectTerminal.getClass().getName());
        };

        return selector.compile();
    }
}
