package org.litebridgedb.orm.api.select;

import org.jspecify.annotations.Nullable;

/**
 * Generic condition clause for building conditional expressions in a fluent and type-safe manner.
 * <p>
 * This interface defines a set of methods for specifying various relational conditions such as equality,
 * inequality, comparison, and null checks. These methods return instances of {@link ConditionClauseTerminal}
 * to allow further chaining and construction of compound conditions.
 *
 * @param <DTO>  the type of the data transfer object associated with the query
 * @param <SELF> the type of the implementing subclass to allow type-safe chaining
 * @param <CCT>  the type of the terminal condition clause returned for further chaining
 */
public interface ConditionClause<DTO,
        SELF extends ConditionClause<DTO, SELF, CCT>,
        CCT extends ConditionClauseTerminal<DTO, SELF, CCT>> {

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
     * Creates a condition terminal for less-than comparison with the specified rhs.
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
