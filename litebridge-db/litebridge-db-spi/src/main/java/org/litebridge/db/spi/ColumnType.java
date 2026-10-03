package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ColumnType {

    protected final int dataType;
    protected final @Nullable Integer size;

    public ColumnType(final int dataType, final @Nullable Integer size) {
        this.dataType = dataType;
        this.size = size;
    }

    public int dataType() {
        return dataType;
    }

    public @Nullable Integer size() {
        return size;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ColumnType that)) return false;
        return dataType == that.dataType && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataType, size);
    }
}
