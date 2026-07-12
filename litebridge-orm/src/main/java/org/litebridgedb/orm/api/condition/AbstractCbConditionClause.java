package org.litebridgedb.orm.api.condition;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.ConditionClause;
import org.litebridgedb.orm.api.select.ConditionClauseTerminal;
import org.litebridgedb.orm.api.select.SelectTerminal;
import org.litebridgedb.orm.api.select.impl.SelectorInspector;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.engine.FromClauseEngine;
import org.litebridgedb.orm.engine.SelectEngine;

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
     * The condition specification being built.
     */
    protected final ConditionSpec conditionSpec;

    /**
     * The condition group specification.
     */
    protected final ConditionGroupSpec conditionGroupSpec;

    /**
     * The engine used to process the FROM clause.
     */
    protected final FromClauseEngine fromClauseEngine;

    /**
     * Constructs a new {@code AbstractCbConditionClause}.
     *
     * @param conditionSpec      The condition specification.
     * @param conditionGroupSpec The condition group specification.
     * @param fromClauseEngine   The FROM clause engine.
     */
    public AbstractCbConditionClause(final ConditionSpec conditionSpec,
                                     final ConditionGroupSpec conditionGroupSpec,
                                     final FromClauseEngine fromClauseEngine) {
        this.conditionSpec = conditionSpec;
        this.conditionGroupSpec = conditionGroupSpec;
        this.fromClauseEngine = fromClauseEngine;
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
        return createCbConditionClauseTerminal();
    }

    /**
     * Creates a new terminal condition clause instance.
     *
     * @return A new {@link AbstractCbConditionClauseTerminal} instance.
     */
    protected abstract AbstractCbConditionClauseTerminal<DTO> createCbConditionClauseTerminal();

    private SelectSpec createSelectSpec(final @Nullable Function<SelectEngine, SelectTerminal<?>> subselect) {
        final SelectTerminal<?> selectTerminal = Objects.requireNonNull(subselect, "Subselect cannot be null")
                .apply(new SelectEngine(fromClauseEngine));
        return SelectorInspector.getSelectSpec(selectTerminal);
    }
}
