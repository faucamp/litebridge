package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

    public String[] fieldPathArray() {
        return fieldPath.split("\\.");
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
                return value;
            }

            value = fieldAccessor.get(value);
        }

        return value;
    }

    @Override
    public void set(final Object dto, final @Nullable Object value) {
        fieldAccessors.getLast().set(dto, value);
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
        return fieldAccessors.getLast().equals(o);
    }

    @Override
    public int hashCode() {
        return fieldAccessors.getLast().hashCode();
    }
}
