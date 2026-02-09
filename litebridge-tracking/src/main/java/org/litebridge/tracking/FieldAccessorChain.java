package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a chain of {@link FieldAccessor} instances, allowing the traversal and manipulation of nested fields
 * of a data transfer object (DTO).
 * <p>
 * This class provides mechanisms to navigate nested structures while enabling read/write access to the chained fields.
 */
public class FieldAccessorChain implements FieldAccessor {

    private final String fieldPath;
    private final List<FieldAccessor> fieldAccessors;

    public FieldAccessorChain(final FieldAccessor parent, final String fieldPath) {
        this.fieldPath = fieldPath;
        this.fieldAccessors = new ArrayList<>();
        this.fieldAccessors.add(parent);
    }

    private FieldAccessorChain(final List<FieldAccessor> fieldAccessors, final String fieldPath) {
        this.fieldAccessors = fieldAccessors;
        this.fieldPath = fieldPath;
    }

    public String fieldPath() {
        return fieldPath;
    }

    public List<FieldAccessor> fieldAccessors() {
        return fieldAccessors;
    }

    public FieldAccessorChain subChain() {
        return new FieldAccessorChain(fieldAccessors.subList(1, fieldAccessors.size()), fieldPath.substring(fieldPath.lastIndexOf('.') + 1));
    }

    public FieldAccessorChain add(final FieldAccessor fieldAccessor) {
        if (fieldAccessor instanceof FieldAccessorChain fieldAccessorChain) {
            fieldAccessors.addAll(fieldAccessorChain.fieldAccessors);
        } else {
            fieldAccessors.add(fieldAccessor);
        }

        return this;
    }

    public boolean isLast(final FieldAccessor fieldAccessor) {
        return fieldAccessors.getLast().equals(fieldAccessor);
    }

    @Override
    public String name() {
        return fieldAccessors.getLast().name();
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

            if (fieldAccessorIterator.hasNext() && ClassFieldAccessorCache.isNestedDtoField(currentDto.getClass(), fieldAccessor)) {
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
        if (!(o instanceof FieldAccessor)) return false;
        return fieldAccessors.getLast().equals(o);
    }

    @Override
    public int hashCode() {
        return fieldAccessors.getLast().hashCode();
    }
}
