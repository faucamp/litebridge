package org.litebridge.orm.api.select;

import org.jspecify.annotations.Nullable;

public interface ConditionClause<DTO, CCT extends ConditionClauseTerminal<DTO, CCT>> {

    /**
     * Equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    CCT eq(final @Nullable Object value);

    /**
     * Not equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    CCT neq(final @Nullable Object value);

    /**
     * Creates a condition terminal for less-than comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    CCT lt(final Object value);

    /**
     * Less than or equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    CCT lte(final Object value);

    /**
     * Greater than
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    CCT gt(final Object value);

    /**
     * Greater than or equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    CCT gte(final Object value);

    /**
     * Null comparison.
     * <p>
     * Equivalent to {@code eq(null)}.
     *
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    CCT isNull();

    /**
     * Not null comparison.
     * <p>
     * Equivalent to {@code neq(null)}.
     *
     * @return A {@link ConditionClauseTerminal} instance for further chaining.
     */
    CCT isNotNull();

}
