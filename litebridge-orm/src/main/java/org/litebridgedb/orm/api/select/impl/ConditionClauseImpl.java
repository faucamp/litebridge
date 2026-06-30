package org.litebridgedb.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.ConditionClause;
import org.litebridgedb.orm.api.select.ConditionClauseTerminal;
import org.litebridgedb.orm.api.select.SelectTerminal;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.engine.SelectEngine;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class ConditionClauseImpl<DTO,
        SELF extends ConditionClause<DTO, SELF, CCT>,
        CCT extends ConditionClauseTerminal<DTO, SELF, CCT>>

        implements ConditionClause<DTO, SELF, CCT> {

    private final ConditionSpec conditionSpec;
    private final CCT conditionTerminal;
    private final LitebridgeContext litebridgeContext;

    public ConditionClauseImpl(final ConditionSpec conditionSpec, final CCT conditionTerminal, final LitebridgeContext litebridgeContext) {
        this.conditionSpec = conditionSpec;
        this.conditionTerminal = conditionTerminal;
        this.litebridgeContext = litebridgeContext;
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

    /**
     * Equals
     *
     * @param subselect Function that builds a sub-select query
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT eq(final Function<SelectEngine, SelectTerminal<?>> subselect) {
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
    public CCT neq(final Function<SelectEngine, SelectTerminal<?>> subselect) {
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
    public CCT lt(final Function<SelectEngine, SelectTerminal<?>> subselect) {
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
    public CCT lte(final Function<SelectEngine, SelectTerminal<?>> subselect) {
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
    public CCT gt(final Function<SelectEngine, SelectTerminal<?>> subselect) {
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
    public CCT gte(final Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.GTE, subselect, false);
    }

    /**
     * Inclusion in a set.
     *
     * @param value       First value that is part of the set
     * @param otherValues Other values that are part of the set
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
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
    public CCT in(final Function<SelectEngine, SelectTerminal<?>> subselect) {
        return subselectImpl(Operator.IN, subselect, false);
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
        conditionSpec.setValue(value);
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

        conditionSpec.setOperator(translatedOperator);
        return conditionTerminal;
    }

    private SelectSpec createSelectSpec(final Function<SelectEngine, SelectTerminal<?>> subselect) {
        final SelectTerminal<?> selectTerminal = Objects.requireNonNull(subselect, "Subselect cannot be null")
                .apply(new SelectEngine(litebridgeContext.fromClauseEngine()));
        return getSelectSpec(selectTerminal);
    }

    private SelectSpec getSelectSpec(final SelectTerminal<?> selectTerminal) {
        if (selectTerminal instanceof AbstractWhereClauseTerminal<?, ?, ?, ?, ?, ?, ?> terminal) {
            return terminal.delegate.selectSpec();
        } else {
            throw new IllegalArgumentException("Unsupported terminal: " + selectTerminal);
        }
    }
}
