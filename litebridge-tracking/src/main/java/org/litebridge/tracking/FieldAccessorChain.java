package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a chain of {@link FieldAccessor} instances, allowing the traversal and manipulation of nested fields
 * of a data transfer object (DTO).
 * <p>
 * This class provides mechanisms to navigate nested structures while enabling read/write access to the chained fields.
 */
public final class FieldAccessorChain implements FieldAccessor {

    private final String fieldPath;
    private final List<FieldAccessor> fieldAccessors;
    private final ClassFieldAccessorCache classFieldAccessorCache;

    /**
     * Constructs a new {@code FieldAccessorChain}.
     *
     * @param parent                  the parent field accessor.
     * @param fieldPath               the field path.
     * @param classFieldAccessorCache the field accessor cache.
     */
    public FieldAccessorChain(final FieldAccessor parent, final String fieldPath, final ClassFieldAccessorCache classFieldAccessorCache) {
        this.fieldPath = fieldPath;
        this.fieldAccessors = new ArrayList<>();
        this.fieldAccessors.add(parent);
        this.classFieldAccessorCache = classFieldAccessorCache;
    }

    private FieldAccessorChain(final List<FieldAccessor> fieldAccessors, final String fieldPath, final ClassFieldAccessorCache classFieldAccessorCache) {
        this.fieldAccessors = fieldAccessors;
        this.fieldPath = fieldPath;
        this.classFieldAccessorCache = classFieldAccessorCache;
    }

    /**
     * Returns the field path.
     *
     * @return the field path.
     */
    public String fieldPath() {
        return fieldPath;
    }

    /**
     * Returns the field accessors.
     *
     * @return the field accessors.
     */
    public List<FieldAccessor> fieldAccessors() {
        return fieldAccessors;
    }

    /**
     * Returns a sub-chain of this field accessor chain.
     *
     * @return a sub-chain of this field accessor chain.
     */
    public FieldAccessorChain subChain() {
        return new FieldAccessorChain(fieldAccessors.subList(1, fieldAccessors.size()), fieldPath.substring(fieldPath.lastIndexOf('.') + 1), classFieldAccessorCache);
    }

    /**
     * Adds a field accessor to the chain.
     *
     * @param fieldAccessor the field accessor to add.
     * @return this field accessor chain.
     */
    public FieldAccessorChain add(final FieldAccessor fieldAccessor) {
        if (fieldAccessor instanceof FieldAccessorChain fieldAccessorChain) {
            fieldAccessors.addAll(fieldAccessorChain.fieldAccessors);
        } else {
            fieldAccessors.add(fieldAccessor);
        }

        return this;
    }

    /**
     * Returns {@code true} if the given field accessor is the last one in the chain.
     *
     * @param fieldAccessor the field accessor to check.
     * @return {@code true} if the given field accessor is the last one in the chain.
     */
    public boolean isLast(final FieldAccessor fieldAccessor) {
        return fieldAccessors.getLast().equals(fieldAccessor);
    }

    @Override
    public String name() {
        return fieldPath;
    }

    @Override
    public @Nullable Object get(final Object dto) {
        Object value = dto;

        // Traverse the field accessors and get the chained value
        for (FieldAccessor fieldAccessor : fieldAccessors) {
            if (value == null) {
                return null;
            }

            value = fieldAccessor.get(value);
        }

        return value;
    }

    @Override
    public void set(final Object dto, final @Nullable Object value) {
        // Traverse the field accessors and set the chained value
        Object currentDto = dto;
        final Iterator<FieldAccessor> fieldAccessorIterator = fieldAccessors.iterator();

        while (fieldAccessorIterator.hasNext()) {
            final FieldAccessor fieldAccessor = fieldAccessorIterator.next();

            if (fieldAccessorIterator.hasNext() && classFieldAccessorCache.isNestedDtoField(currentDto.getClass(), fieldAccessor)) {
                final Object intermediateValue = fieldAccessor.get(currentDto);

                if (intermediateValue == null) {
                    // Create intermediate entity and set it
                    final Object newEntity = ClassUtils.newInstance(fieldAccessor.type());
                    fieldAccessor.set(currentDto, newEntity);
                    currentDto = newEntity;
                } else {
                    currentDto = intermediateValue;
                }
            } else if (fieldAccessor.dtoClass() == currentDto.getClass()) {
                fieldAccessor.set(currentDto, value);
            } else {
                throw new IllegalArgumentException("Cannot set field path '" + fieldPath + "' on DTO of type: " + dto.getClass().getName());
            }
        }
    }

    @Override
    public Class<?> type() {
        return fieldAccessors.getLast().type();
    }

    @Override
    public Class<?>[] genericTypes() {
        return fieldAccessors.getLast().genericTypes();
    }

    @Override
    public Class<?> dtoClass() {
        return fieldAccessors.getLast().dtoClass();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final FieldAccessorChain that)) return false;
        return Objects.equals(fieldPath, that.fieldPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldPath);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FieldAccessorChain.class.getSimpleName() + "[", "]")
                .add("fieldPath='" + fieldPath + "'")
                .toString();
    }
}
