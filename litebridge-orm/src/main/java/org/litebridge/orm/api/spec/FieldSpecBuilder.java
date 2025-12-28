package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;

public final class FieldSpecBuilder implements FieldSpec {

    /**
     * Built FieldSpec instance
     */
    @Nullable
    private FieldSpecImpl fieldSpec;

    /**
     * Field/property name
     */
    private final String name;
    /**
     * Whether the field is accessed as a property instead of direct field access
     */
    private boolean property;

    private FieldSpecBuilder(final String name) {
        this.name = name;
    }

    public FieldSpecBuilder property(final boolean property) {
        this.property = property;
        return this;
    }

    /**
     * Creates a new {@code FieldSpecBuilder} configured with the specified field/property name.
     *
     * @param field the name of the field/property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    public static FieldSpecBuilder f(final String field) {
        return new FieldSpecBuilder(field);
    }

    /**
     * Creates a new {@code FieldSpecBuilder} configured for property access of the specified property name.
     * Equivalent to {@code FieldSpecBuilder(property).property(true)}
     *
     * @param property the name of the property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    public static FieldSpecBuilder p(final String property) {
        return new FieldSpecBuilder(property).property(true);
    }

    @Override
    public String name() {
        return ensureFieldSpec().name();
    }

    @Override
    public boolean property() {
        return ensureFieldSpec().property();
    }

    private FieldSpecImpl ensureFieldSpec() {
        if (fieldSpec == null) {
            fieldSpec = new FieldSpecImpl(name, property);
        }

        return fieldSpec;
    }
}
