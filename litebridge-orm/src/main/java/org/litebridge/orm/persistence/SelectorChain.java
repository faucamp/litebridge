package org.litebridge.orm.persistence;

import jakarta.annotation.Nullable;
import org.litebridge.orm.exception.NonUniqueResultException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
public interface SelectorChain<T> {

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
    OrderByChain<T> orderBy(final String... columns);

    /**
     * Applies a result offset (typically translated to {@code OFFSET}).
     *
     * @param offset number of rows to skip; must be {@code >= 0}
     * @return a selector chain with the offset applied
     */
    SelectorChain<T> offset(final int offset);

    /**
     * Applies a maximum number of rows to return (typically translated to {@code LIMIT}).
     *
     * @param limit maximum number of rows; must be {@code >= 0}
     * @return a selector chain with the limit applied
     */
    SelectorChain<T> limit(final int limit);

    /**
     * Executes the query and expects exactly one result.
     * <p>
     * The returned {@link Optional} is empty when no row matches. If more than one row matches,
     * the underlying implementation is expected to fail (typically by throwing an exception).
     *
     * @return an {@link Optional} containing the single result, if present
     */
    Optional<T> one();

    /**
     * Executes the query and expects exactly one result.
     *
     * @return the single result, or {@code null} when no row matches
     * @throws NonUniqueResultException if more than one row matches
     */
    @Nullable
    T oneOrNull() throws NonUniqueResultException;

    /**
     * Executes the query and expects exactly one result.
     *
     * @return the single result
     * @throws RuntimeException if no row matches or more than one row matches
     */
    T oneOrThrow() throws NoSuchElementException;

    /**
     * Executes the query and expects exactly one result.
     * <p>
     * When the result is not exactly one row, the supplied exception is thrown.
     *
     * @param exceptionSupplier supplier used to create the exception to throw when the result is not exactly one row
     * @param <X>               exception type
     * @return the single result
     * @throws X if no row matches or more than one row matches
     */
    <X extends Throwable> T oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X;

    /**
     * Executes the query and returns the first row if present.
     * <p>
     * Unlike {@link #one()}, this method does not require uniqueness; if multiple rows match,
     * only the first is returned (according to the effective ordering, if any).
     *
     * @return an {@link Optional} with the first result, if present
     */
    Optional<T> first();

    /**
     * Executes the query and returns the first row if present.
     *
     * @return the first result, or {@code null} when no row matches
     */
    @Nullable
    T firstOrNull();

    /**
     * Executes the query and returns the first row.
     *
     * @return the first result
     * @throws NoSuchElementException if no row matches
     */
    T firstOrThrow() throws NoSuchElementException;

    /**
     * Executes the query and returns the first row.
     * <p>
     * When no row matches, the supplied exception is thrown.
     *
     * @param exceptionSupplier supplier used to create the exception to throw when no row matches
     * @param <X>               exception type
     * @return the first result
     * @throws X if no row matches
     */
    <X extends Throwable> T firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X;

    /**
     * Executes the query and returns results as a {@link Stream}.
     * <p>
     * Implementations may tie the stream to underlying resources (for example a JDBC {@code ResultSet}).
     * Prefer using try-with-resources (or otherwise ensuring the stream is closed) if the returned
     * stream is {@link AutoCloseable} via {@link Stream#close()}.
     *
     * @return a stream of results
     */
    Stream<T> stream();

    /**
     * Executes the query and materializes all results into a {@link List}.
     *
     * @return list of all matching results (possibly empty)
     */
    List<T> list();
}
