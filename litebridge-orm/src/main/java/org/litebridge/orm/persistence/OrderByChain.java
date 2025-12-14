package org.litebridge.orm.persistence;

/**
 * The OrderByChain interface provides methods for specifying the sorting
 * of query results in ascending or descending order. It is designed to be
 * chained with other query-building components, enabling the construction
 * of complex query conditions fluently.
 *
 * @param <T>  The type of entity or data being queried.
 * @param <CT> The type of the condition terminal associated with the query.
 */
public interface OrderByChain<T, CT extends ConditionTerminal<T, CT>> {

    /**
     * Specifies an ascending order for the current ordering clause in a query.
     * This method is part of a fluent interface for building query conditions.
     *
     * @return an {@link OrderByTerminal} instance with ascending order applied
     */
    OrderByTerminal<T, CT> asc();

    /**
     * Specifies an descending order for the current ordering clause in a query.
     * This method is part of a fluent interface for building query conditions.
     *
     * @return an {@link OrderByTerminal} instance with descending order applied
     */
    OrderByTerminal<T, CT> desc();
}
