package org.litebridge.orm.persistence;

public final class OrderByClosure<T> extends DelegatingSelectorChain<T> {

    public OrderByClosure(final Selector<T> selector) {
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
    public OrderByChain<T> then(final String... columns) {
        return selector.orderBy(columns);
    }
}
