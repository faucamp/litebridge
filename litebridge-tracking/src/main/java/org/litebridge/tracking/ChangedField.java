package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public sealed class ChangedField permits ChangedMapField {

    protected final String name;

    @Nullable
    protected final Object value;

    public ChangedField(String name, @Nullable Object value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public Object value() {
        return value;
    }

    public <T extends ChangedField> Optional<T> cast(Class<T> changedFieldType) {
        return Optional.of(changedFieldType.cast(this));
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final ChangedField that = (ChangedField) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ChangedField.class.getSimpleName() + "[", "]")
                .add("fieldName='" + name + "'")
                .add("value=" + value)
                .toString();
    }
}
