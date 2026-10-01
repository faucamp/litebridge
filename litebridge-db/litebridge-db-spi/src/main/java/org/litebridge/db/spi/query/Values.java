package org.litebridge.db.spi.query;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.VirtualTable;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public final class Values extends VirtualTable {

    private final String[] labels;
    private final @Nullable Object[] values;

    public Values(final String alias, final String[] labels, final @Nullable Object[] values) {
        super(alias);
        this.labels = labels;
        this.values = values;
    }

    public String[] labels() {
        return labels;
    }

    public @Nullable Object[] values() {
        return values;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Values values1)) return false;
        if (!super.equals(o)) return false;
        return Objects.deepEquals(labels, values1.labels) && Objects.deepEquals(values, values1.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(labels), Arrays.hashCode(values));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Values.class.getSimpleName() + "[", "]")
                .add("alias='" + name() + "'")
                .add("labels=" + Arrays.toString(labels))
                .add("values=" + Arrays.toString(values))
                .toString();
    }
}
