package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * A field which has undergone a change.
 * <p>
 * This is a sealed class that can be extended by specific implementations
 * to handle particular types of field changes.
 * <p>
 * The class stores the name of the changed field and its value, and provides
 * utilities for accessing and comparing these fields.
 */
public sealed class ChangedField permits ChangedCollectionField, ChangedMapField {

    protected final String name;

    @Nullable
    protected final Object value;

    /**
     * Construct a new instance of {@code ChangedField}, representing a change in a specific field.
     *
     * @param name  the name of the field that has changed; must not be null
     * @param value the current value of the field that has changed; may be null
     */
    public ChangedField(String name, @Nullable Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Retrieve the name of the field that has changed.
     *
     * @return the name of the changed field
     */
    public String name() {
        return name;
    }

    /**
     * Retrieve the current value of the field that has changed.
     *
     * @return the current value of the changed field, which may be {@code null}
     */
    public @Nullable Object value() {
        return value;
    }

    /**
     * Attempts to cast this {@code ChangedField} instance to a specific subtype.
     * This provides a convenient type-safe way to cast the field to a specific subtype if it known beforehand.
     * <p>
     * If the cast is possible, the corresponding subtype instance is returned wrapped in an {@code Optional}.
     * If the cast fails, an empty {@code Optional} is returned.
     *
     * @param <T>              the type of the desired subtype of {@code ChangedField}
     * @param changedFieldType the {@code Class} object representing the desired subtype
     * @return an {@code Optional} containing the casted instance if the cast is successful,
     * or an empty {@code Optional} if the cast fails
     */
    public final <T extends ChangedField> Optional<T> cast(Class<T> changedFieldType) {
        final T changedFieldSubtype;

        try {
            changedFieldSubtype = changedFieldType.cast(this);
        } catch (final ClassCastException ex) {
            return Optional.empty();
        }

        return Optional.of(changedFieldSubtype);
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
