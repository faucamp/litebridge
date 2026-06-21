package org.litebridgedb.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.ConditionClause;
import org.litebridgedb.orm.api.select.ConditionClauseTerminal;
import org.litebridgedb.orm.api.select.SelectTerminal;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.SelectSpec;

import java.util.function.Function;

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

    public CCT eq(final Function<Subselect, SelectTerminal<?>> sb) {
        final SelectTerminal<?> selectTerminal = sb.apply(new Subselect(litebridgeContext.fromClauseEngine()));
        final SelectSpec selectSpec = getSelectSpec(selectTerminal);
        return condition(Operator.EQ, selectSpec);
    }

    protected SelectSpec getSelectSpec(final SelectTerminal<?> selectTerminal) {
        if (selectTerminal instanceof AbstractWhereClauseTerminal<?, ?, ?, ?> terminal) {
            return terminal.delegate.selectSpec();
        } else {
            throw new IllegalArgumentException("Unsupported terminal: " + selectTerminal);
        }
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
     * Creates a condition terminal for less-than comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT lt(final Object value) {
        return condition(Operator.LT, value);
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
     * Greater than
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    public CCT gt(final Object value) {
        return condition(Operator.GT, value);
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
}
