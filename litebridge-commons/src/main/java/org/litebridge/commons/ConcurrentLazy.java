package org.litebridge.commons;

import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;


/**
 * A thread-safe, lazily initialized value holder that allows resetting the value.
 * <p>
 * This class ensures that the value is initialized only once, using the provided {@link Supplier},
 * and that subsequent calls to {@link #optional()} or {@link #orNull()} return the cached value.
 * <p>
 * The value can be reset using {@link #reset()}, which clears the cached value and allows it to be recomputed
 * on the next access.
 *
 * @param <T> the type of the lazily initialized value
 */
public final class ConcurrentLazy<T> {

    private static final Object UNINITIALIZED = new Object();

    @Nullable
    private volatile Object value = UNINITIALIZED;

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
    public Optional<T> optional() {
        return Optional.ofNullable(orNull());
    }

    /**
     * Returns the lazily initialized value. If the value has not yet been initialized,
     * it will be computed using the provided initializer in a thread-safe manner.
     *
     * @return the initialized value
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public T orNull() {
        Object result = value;

        if (result == UNINITIALIZED) {
            synchronized (this) {
                result = value;

                if (result == UNINITIALIZED) {
                    result = initializer.get();
                    value = result;
                }
            }
        }

        return (T) result;
    }

    /**
     * Returns the current value without triggering initialization.
     * Useful for logging or debugging where you don't want to force a side-effect.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public T peek() {
        Object result = value;
        return (result == UNINITIALIZED) ? null : (T) result;
    }

    /**
     * Resets the cached value, allowing it to be recomputed on the next call to {@link #optional()} or {@link #orNull()}.
     * This method is thread-safe.
     */
    public void reset() {
        synchronized (this) {
            value = UNINITIALIZED;
        }
    }

    /**
     * Returns {@code true} if the value has already been initialized, or {@code false} otherwise.
     *
     * @return {@code true} if the value is initialized; {@code false} if it is still uninitialized
     */
    public boolean isInitialized() {
        return value != UNINITIALIZED;
    }
}
