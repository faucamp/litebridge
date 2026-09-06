package org.litebridge.commons.type;

import org.jspecify.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * A thread-safe, lazily initialized value holder that allows resetting the value.
 * <p>
 * This class ensures that the value is initialized only once, using the provided {@link Supplier},
 * and that subsequent calls to {@link #get()} or {@link #getOrNull()} return the cached value.
 * <p>
 * The value can be reset using {@link #reset()}, which clears the cached value and allows it to be recomputed
 * on the next access.
 *
 * @param <T> the type of the lazily initialized value
 */
public final class ConcurrentLazy<T> extends AbstractConcurrentLazy<T> {

    private final Supplier<T> initializer;

    /**
     * Constructs a new {@code ResettableLazy} with the given initializer.
     *
     * @param initializer a {@link Supplier} that provides the value when it is first needed
     * @throws NullPointerException if the initializer is {@code null}
     */
    public ConcurrentLazy(final Supplier<T> initializer) {
        this.initializer = initializer;
    }

    /**
     * The primary accessor. Returns the value wrapped in an {@link Optional}.
     *
     * @return the value wrapped in an {@link Optional}
     */
    public Optional<T> get() {
        return Optional.ofNullable(getOrNull());
    }


    /**
     * Returns the value, or throws a {@link NoSuchElementException} if the value is null.
     *
     * @return the value
     * @throws NoSuchElementException if the value is null
     */
    public T getOrThrow() {
        return getOrThrow(NoSuchElementException::new);
    }

    /**
     * Returns the value, or throws an exception provided by the supplier if the value is null.
     *
     * @param <X>               the type of the exception to be thrown
     * @param exceptionSupplier the supplier of the exception to be thrown
     * @return the value
     * @throws X if the value is null
     */
    public <X extends Exception> T getOrThrow(Supplier<X> exceptionSupplier) throws X {
        final T result = getOrNull();

        if (result == null) {
            throw exceptionSupplier.get();
        }

        return result;
    }

    /**
     * Returns the lazily initialized value. If the value has not yet been initialized,
     * it will be computed using the provided initializer in a thread-safe manner.
     *
     * @return the initialized value
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public T getOrNull() {
        Object result = value;

        if (result == UNINITIALISED) {
            synchronized (this) {
                result = value;

                if (result == UNINITIALISED) {
                    result = initializer.get();
                    value = result;
                }
            }
        }

        return (T) result;
    }
}
