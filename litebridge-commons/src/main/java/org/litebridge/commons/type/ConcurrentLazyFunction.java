package org.litebridge.commons.type;

import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A thread-safe, lazily initialized value holder for values of type {@code R} that require an input of type {@code T}
 * to compute the value.
 * <p>
 * Allows resetting the cached value and ensures initialisation is performed only once in a concurrent environment.
 *
 * @param <T> the type of the input required to compute the value
 * @param <R> the type of the lazily initialised value
 */
public final class ConcurrentLazyFunction<T, R> extends AbstractConcurrentLazy<R> {

    private final Function<T, R> initialiser;

    public ConcurrentLazyFunction(final Function<T, R> initialiser) {
        this.initialiser = initialiser;
    }

    /**
     * The primary accessor. Returns the value wrapped in an {@link Optional}.
     *
     * @return the value wrapped in an {@link Optional}
     */
    public Optional<R> get(final T input) {
        return Optional.ofNullable(getOrNull(input));
    }


    /**
     * Returns the value, or throws a {@link NoSuchElementException} if the value is null.
     *
     * @return the value
     * @throws NoSuchElementException if the value is null
     */
    public R getOrThrow(final T input) {
        return getOrThrow(input, NoSuchElementException::new);
    }

    /**
     * Returns the value, or throws an exception provided by the supplier if the value is null.
     *
     * @param <X>               the type of the exception to be thrown
     * @param exceptionSupplier the supplier of the exception to be thrown
     * @return the value
     * @throws X if the value is null
     */
    public <X extends Exception> R getOrThrow(final T input, Supplier<X> exceptionSupplier) throws X {
        final R result = getOrNull(input);

        if (result == null) {
            throw exceptionSupplier.get();
        }

        return result;
    }

    /**
     * Returns the lazily initialised value. If the value has not yet been initialised,
     * it will be computed using the provided initialiser in a thread-safe manner.
     *
     * @return the initialised value
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public R getOrNull(final T input) {
        Object result = value;

        if (result == UNINITIALISED) {
            synchronized (this) {
                result = value;

                if (result == UNINITIALISED) {
                    result = initialiser.apply(input);
                    value = result;
                }
            }
        }

        return (R) result;
    }
}
