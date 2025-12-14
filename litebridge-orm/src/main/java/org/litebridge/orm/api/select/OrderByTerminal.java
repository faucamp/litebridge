package org.litebridge.orm.api.select;

/**
 * The OrderByTerminal class extends the functionality of the DelegatingSelectorChain
 * to support the addition of ordering expressions in a query. It provides methods
 * to specify additional columns for ordering with explicit directions such as
 * ascending or descending order.
 * <p>
 * This class primarily acts as a part of a fluent interface, allowing developers
 * to build queries with multiple chained ordering expressions.
 *
 * @param <T>  The type of DTO or data being queried.
 * @param <CT> The type of the condition terminal associated with the query.
 */
public final class OrderByTerminal<T, CT extends ConditionTerminal<T, CT>> extends DelegatingSelectorChain<T, CT> {

    public OrderByTerminal(final Selector<T, CT> selector) {
        super(selector);
    }

    /**
     * Adds another ordering expression which again requires an explicit direction.
     * Each call to this method appends another ordering expression.
     * <p>
     * This keeps parity with {@link Selector#orderBy(String...)}.
     *
     * @param columns Table column(s) or DTO field(s) to order by
     * @return a selector chain with ordering applied
     */
    public OrderByChain<T, CT> then(final String... columns) {
        return selector.orderBy(columns);
    }
}
