package org.litebridgedb.orm.api.spec;

/**
 * Defines a mapping of a field or a property in a class to its corresponding representation.
 * <p>
 * This sealed interface serves as a contract for different types of field or property mappings
 * in the API. It can represent either a specific field or property mapping, or signify the absence
 * of any mapping.
 * <p>
 * The implementations of this interface are:
 * - {@link FieldSpec}: Represents a specification for mapping a particular field or property.
 * - {@link NoFieldMapping}: Represents a state where no field mapping is defined.
 */
public sealed interface FieldMapping permits FieldSpec, NoFieldMapping {

    /**
     * Shortcut for {@link #field(String)}.
     * Creates a new {@link FieldSpec} configured for field-level access of the specified field name.
     *
     * @param field the name of the field/property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    static FieldSpec f(final String field) {
        return field(field);
    }

    /**
     * Creates a new {@link FieldSpec} configured for field-level access of the specified field name.
     *
     * @param field the name of the field/property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    static FieldSpec field(final String field) {
        return new FieldSpec(field, false);
    }

    /**
     * Shortcut for {@link #property(String)}.
     * Creates a new {@code FieldSpecBuilder} configured for property access of the specified property name.
     *
     * @param property the name of the property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    static FieldSpec p(final String property) {
        return property(property);
    }

    /**
     * Creates a new {@code FieldSpecBuilder} configured for property access of the specified property name.
     *
     * @param property the name of the property in a class
     * @return this {@code FieldSpecBuilder} for further chaining
     */
    static FieldSpec property(final String property) {
        return new FieldSpec(property, true);
    }
}
