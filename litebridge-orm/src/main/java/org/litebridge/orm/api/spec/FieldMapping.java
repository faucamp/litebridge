package org.litebridge.orm.api.spec;

public final class FieldMapping {

    private FieldMapping() {
    }

    /**
     * Shortcut for {@link #field(String)}.
     * Creates a new {@link FieldSpec} configured for field-level access of the specified field name.
     *
     * @param field the name of the field/property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    public static FieldSpec f(final String field) {
        return field(field);
    }

    /**
     * Creates a new {@link FieldSpec} configured for field-level access of the specified field name.
     *
     * @param field the name of the field/property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    public static FieldSpec field(final String field) {
        return new FieldSpec(field, false);
    }

    /**
     * Shortcut for {@link #property(String)}.
     * Creates a new {@code FieldSpecBuilder} configured for property access of the specified property name.
     *
     * @param property the name of the property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    public static FieldSpec p(final String property) {
        return property(property);
    }

    /**
     * Creates a new {@code FieldSpecBuilder} configured for property access of the specified property name.
     *
     * @param property the name of the property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    public static FieldSpec property(final String property) {
        return new FieldSpec(property, true);
    }
}
