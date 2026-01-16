package org.litebridge.tracking;

import org.jspecify.annotations.Nullable;

/**
 * Abstraction for accessing and manipulating fields or properties of a data transfer object (DTO).
 * <p>
 * Enables getting and setting field values, retrieving metadata about the field, such as its name,
 * type, generic types, and declaring class.
 */
public interface FieldAccessor {

    /**
     * Return the name of the field or property represented by this {@code FieldAccessor}.
     *
     * @return the name of the field or property as a {@code String}
     */
    String name();

    /**
     * Returns a {@code Class} object that identifies the declared type for the field represented by this {@code FieldAccessor} object.
     *
     * @return a {@code Class} object identifying the declared type of the field represented by this object
     */
    Class<?> type();

    /**
     * Retrieves the {@code Class} object representing the data transfer object (DTO) type
     * associated with this {@code FieldAccessor}.
     *
     * @return a {@code Class} object representing the data transfer object type
     */
    Class<?> dtoClass();

    /**
     * Retrieve the value of a field or property from the provided data transfer object (DTO).
     *
     * @param dto the data transfer object from which to retrieve the field or property value.
     *            Must not be null and should conform to the structure expected by this {@code FieldAccessor}.
     * @return the value of the field or property, or {@code null} if the field value is null or
     * could not be accessed.
     */
    @Nullable
    Object get(final Object dto);

    /**
     * Set the value of a field or property for the provided data transfer object (DTO).
     *
     * @param dto   the data transfer object for which the field or property value will be set.
     *              Must not be null and should conform to the structure expected by this {@code FieldAccessor}.
     * @param value the value to set for the field or property. May be null if the field or property allows null values.
     */
    void set(final Object dto, final @Nullable Object value);

    /**
     * Returns an array of {@code Class} objects representing the generic types associated with
     * the field or property represented by this {@code FieldAccessor}.
     *
     * @return an array of {@code Class} objects that represent the generic types of the field or property,
     * or an empty array if the field or property does not use generics.
     */
    Class<?>[] genericTypes();

    /**
     * Retrieves the single generic type associated with the field or property
     * represented by this {@code FieldAccessor}.
     * <p>
     * If the field or property has more than one generic type or none at all,
     * an {@code IllegalStateException} is thrown.
     *
     * @return the {@code Class} object representing the single generic type of the field or property
     * @throws IllegalStateException if the number of generic types is not exactly one
     */
    default Class<?> genericType() {
        final Class<?>[] genericTypes = genericTypes();

        if (genericTypes.length != 1) {
            throw new IllegalStateException("Expected exactly one generic type, but got " + genericTypes.length);
        }

        return genericTypes[0];
    }
}
