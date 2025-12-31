package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public final class ChangedMapField extends ChangedField {

    private final Map<?, Integer> mapSnapshot;

    public ChangedMapField(final String fieldName, final Object value, final Map<?, Integer> mapSnapshot) {
        super(fieldName, value);
        this.mapSnapshot = Collections.unmodifiableMap(mapSnapshot);
    }

    public Map<?, Integer> mapSnapshot() {
        return mapSnapshot;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final ChangedMapField that = (ChangedMapField) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.value, that.value) &&
                Objects.equals(this.mapSnapshot, that.mapSnapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, mapSnapshot);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ChangedMapField.class.getSimpleName() + "[", "]")
                .add("fieldName='" + name + "'")
                .add("value=" + value)
                .add("mapSnapshot=" + mapSnapshot)
                .toString();
    }
}
