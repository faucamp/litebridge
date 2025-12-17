package org.litebridge.commons;

import java.util.function.Supplier;


/**
 * A thread-safe, lazily initialized value holder that allows resetting the value.
 * <p>
 * This class ensures that the value is initialized only once, using the provided {@link Supplier},
 * and that subsequent calls to {@link #get()} return the cached value. The value can be reset
 * using {@link #reset()}, which clears the cached value and allows it to be recomputed on the next access.
 *
 * @param <T> the type of the lazily initialized value
 */
public final class ConcurrentLazy<T> {
    private volatile T value;
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
     * Returns the lazily initialized value. If the value has not yet been initialized,
     * it will be computed using the provided initializer in a thread-safe manner.
     *
     * @return the initialized value
     */
    public T get() {
        T result = value;
        if (result == null) {
            synchronized (this) {
                result = value;
                if (result == null) {
                    result = initializer.get();
                    value = result;
                }
            }
        }
        return result;
    }


    /**
     * Resets the cached value, allowing it to be recomputed on the next call to {@link #get()}.
     * This method is thread-safe.
     */
    public void reset() {
        synchronized (this) {
            value = null;
        }
    }


    /**
     * Returns {@code true} if the value has already been initialized, or {@code false} otherwise.
     *
     * @return {@code true} if the value is initialized; {@code false} if it is still uninitialized
     */
    public boolean isInitialized() {
        return value != null;
    }
}
