package org.litebridge.orm.persistence.manytomany;

import org.jspecify.annotations.Nullable;
import org.litebridge.tracking.FieldAccessor;

public final class NoOpFieldAccessor implements FieldAccessor {

    @Override
    public String name() {
        return "no-op";
    }

    @Override
    public Class<?> type() {
        return Object.class;
    }

    @Override
    public Class<?> dtoClass() {
        return NoOpFieldAccessor.class;
    }

    @Override
    public @Nullable Object get(final Object dto) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(final Object dto, final @Nullable Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?>[] genericTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> genericType() {
        throw new UnsupportedOperationException();
    }
}
