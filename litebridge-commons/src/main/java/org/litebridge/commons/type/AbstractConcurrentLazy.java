package org.litebridge.commons.type;

import org.jspecify.annotations.Nullable;

sealed class AbstractConcurrentLazy<T> permits ConcurrentLazy, ConcurrentLazyFunction {

    protected static final Object UNINITIALISED = new Object();

    @Nullable
    protected volatile Object value = UNINITIALISED;

    /**
     * Returns the current value without triggering initialisation.
     * <p>
     * Useful for logging or debugging without forcing a side effect.
     *
     * @return the current value, or {@code null} if not yet initialised
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public final T peek() {
        Object result = value;
        return (result == UNINITIALISED) ? null : (T) result;
    }

    /**
     * Resets the cached value.
     * <p>
     * This allows the cache to be recomputed on the next call to {@code get()} or {@code getOrNull()}.
     */
    public final void reset() {
        synchronized (this) {
            value = UNINITIALISED;
        }
    }

    /**
     * Returns {@code true} if the value has already been initialised, or {@code false} otherwise.
     *
     * @return {@code true} if the value is initialised; {@code false} if it is still uninitialised
     */
    public final boolean isInitialised() {
        return value != UNINITIALISED;
    }
}
