package org.litebridge.orm.persistence;

import jakarta.annotation.Nullable;
import org.litebridge.orm.exception.NonUniqueResultException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface SelectorTerminal<T> {

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
