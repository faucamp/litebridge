package org.litebridgedb.orm.api.select;

import org.litebridgedb.orm.api.spec.FieldColumnSpec;

/**
 * Terminal interface for building SQL condition clauses in a fluent and type-safe manner.
 * <p>
 * This interface is designed to provide methods that mark the end of a condition clause and allow further
 * chaining of conditions.
 * <p>
 * The use of generics ensures type safety and supports fluent query construction by transitioning
 * between different stages of the query.
 *
 * @param <DTO>  the data transfer object (DTO) type that represents the result of the query
 * @param <CC>   the type of the parent condition clause interface to allow additional chaining
 * @param <SELF> the type of the implementing subclass to enable type-safe fluent APIs
 */
public interface ConditionClauseTerminal<DTO,
        CC extends ConditionClause<DTO, CC, SELF>,
        SELF extends ConditionClauseTerminal<DTO, CC, SELF>> {

    /**
     * Adds an "AND" condition to the current condition clause using the specified lhs.
     * This method is used to chain additional conditions in a SQL query in a fluent manner.
     *
     * @param column the name of the lhs to be used in the "AND" condition
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    CC and(String column);

    /**
     * Adds an "AND" condition to the current condition clause using the specified lhs.
     * This method is used to chain additional conditions in a SQL query in a type-safe and fluent manner.
     *
     * @param column the lhs to be used in the "AND" condition
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    CC and(FieldColumnSpec column);
}
