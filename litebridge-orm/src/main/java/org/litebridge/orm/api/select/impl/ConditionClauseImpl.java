package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.ConditionClause;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngine;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class ConditionClauseImpl<DTO,
        SELF extends ConditionClause<DTO, SELF, CCT>,
        CCT extends ConditionClauseTerminal<DTO, SELF, CCT>>

        implements ConditionClause<DTO, SELF, CCT> {

    private final LitebridgeContext litebridgeContext;
    private final Function<QueryNode, CCT> terminalRecreator;
    private final LogicOperator logicOperator;
    private final ExpressionSpec lhs;
    private final @Nullable QueryNode node;

    public ConditionClauseImpl(final LitebridgeContext litebridgeContext,
                               final LogicOperator logicOperator,
                               final ExpressionSpec lhs,
                               final @Nullable QueryNode node,
                               final Function<QueryNode, CCT> terminalRecreator) {
        this.litebridgeContext = litebridgeContext;
        this.logicOperator = logicOperator;
        this.lhs = lhs;
        this.node = node;
        this.terminalRecreator = terminalRecreator;
    }

    /**
     * Equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT eq(final @Nullable Object value) {
        return condition(Operator.EQ, value);
    }

    public CCT using(final String column) {
        final QueryNode newNode = new ConditionNode(node, LogicOperator.NOOP, null, Operator.USING, column, null);
        return terminalRecreator.apply(newNode);
    }

    /**
     * Equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT eq(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.EQ, subselect, true);
    }

    /**
     * Not equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT neq(final @Nullable Object value) {
        return condition(Operator.NEQ, value);
    }

    /**
     * Not equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT neq(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.NEQ, subselect, true);
    }

    /**
     * Less than
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT lt(final Object value) {
        return condition(Operator.LT, Objects.requireNonNull(value, "Operator LT requires a non-NULL RHS value"));
    }

    /**
     * Less than
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT lt(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.LT, subselect, false);
    }

    /**
     * Less than or equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT lte(final Object value) {
        return condition(Operator.LTE, value);
    }

    /**
     * Less than or equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT lte(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.LTE, subselect, false);
    }

    /**
     * Greater than
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT gt(final Object value) {
        return condition(Operator.GT, value);
    }

    /**
     * Greater than
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT gt(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.GT, subselect, false);
    }

    /**
     * Greater than or equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT gte(final Object value) {
        return condition(Operator.GTE, value);
    }

    /**
     * Greater than or equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT gte(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.GTE, subselect, false);
    }

    /**
     * Like
     *
     * @param value The operand for the like expression.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    @Override
    public CCT like(final String value) {
        return condition(Operator.LIKE, value);
    }

    @Override
    public CCT in(final Object value, final Object... otherValues) {
        if (value instanceof Collection<?> collection && otherValues.length == 0) {
            return in(collection);
        }

        return in(Stream.concat(Stream.of(value), Arrays.stream(otherValues)).toList());
    }

    @Override
    public CCT in(final Collection<?> values) {
        return condition(Operator.IN, values);
    }

    @Override
    public CCT in(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.IN, subselect, false);
    }

    @Override
    public CCT notIn(final Object value, final Object... otherValues) {
        if (value instanceof Collection<?> collection && otherValues.length == 0) {
            return notIn(collection);
        }

        return notIn(Stream.concat(Stream.of(value), Arrays.stream(otherValues)).toList());
    }

    @Override
    public CCT notIn(final Collection<?> values) {
        return condition(Operator.NOT_IN, values);
    }

    @Override
    public CCT notIn(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.NOT_IN, subselect, false);
    }

    /**
     * Null comparison.
     * <p>
     * Equivalent to {@code eq(null)}.
     *
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT isNull() {
        return condition(Operator.IS_NULL, null);
    }

    /**
     * Not null comparison.
     * <p>
     * Equivalent to {@code neq(null)}.
     *
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT isNotNull() {
        return condition(Operator.IS_NOT_NULL, null);
    }

    private CCT subselectImpl(final Operator operator,
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
    private CCT condition(final Operator operator, @Nullable final Object value) {
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

        final QueryNode conditionNode = new ConditionNode(node, logicOperator, lhs, translatedOperator, value, null);

        return terminalRecreator.apply(conditionNode);
    }

    private SelectSpec createSelectSpec(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        final SelectTerminal<?> selectTerminal = Objects.requireNonNull(subselect, "Subselect cannot be null")
                .apply(new SelectEngine(litebridgeContext.fromClauseEngine()));
        return getSelectSpec(selectTerminal);
    }

    private SelectSpec getSelectSpec(final SelectTerminal<?> selectTerminal) {
        final AbstractSelector<?, ?> selector = switch (selectTerminal) {
            case DelegatingSelector<?, ?> delegating -> delegating.delegate();
            case AbstractSelector<?, ?> s -> s;
            default ->
                    throw new IllegalArgumentException("Unsupported terminal type: " + selectTerminal.getClass().getName());
        };

        return selector.compile();
    }
}
