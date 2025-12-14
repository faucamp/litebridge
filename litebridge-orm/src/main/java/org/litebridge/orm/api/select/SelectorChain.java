package org.litebridge.orm.api.select;

/**
 * Fluent selector pipeline used to configure and execute a read/query operation.
 * <p>
 * A {@code SelectorChain} typically represents a partially-built query.
 * Configuration methods (for example {@link #orderBy(String...)}, {@link #offset(int)}, {@link #limit(int)})
 * return a logically updated chain instance so calls can be composed:
 * <pre>{@code
 * List<User> users = orm.select(User.class)
 *     .orderBy("created_at")
 *     .offset(50)
 *     .limit(25)
 *     .list();
 * }</pre>
 * <p>
 * Terminal methods (for example {@link #one()}, {@link #first()}, {@link #list()}, {@link #stream()})
 * trigger execution and materialize results.
 *
 * @param <T> result element type produced by this selector
 */
public interface SelectorChain<T, CT extends ConditionTerminal<T, CT>> extends SelectorTerminal<T> {

    /**
     * Applies {@code ORDER BY} for one or more columns/fields, requiring an explicit direction.
     * Each call to this method appends another ordering expression.
     * <p>
     * Specifying multiple columns is a convenient way to apply the same order direction to all:
     * {@code orderBy("a","b").asc()} == {@code orderBy("a").asc().then("b").asc()}.
     *
     * @param columns Table column(s) or DTO field(s) to order by
     * @return a selector chain with ordering applied
     */
    OrderByChain<T, CT> orderBy(final String... columns);

    /**
     * Applies a result offset (typically translated to {@code OFFSET}).
     *
     * @param offset number of rows to skip; must be {@code >= 0}
     * @return a selector chain with the offset applied
     */
    SelectorChain<T, CT> offset(final int offset);

    /**
     * Applies a maximum number of rows to return (typically translated to {@code LIMIT}).
     *
     * @param limit maximum number of rows; must be {@code >= 0}
     * @return a selector chain with the limit applied
     */
    SelectorChain<T, CT> limit(final int limit);
}
