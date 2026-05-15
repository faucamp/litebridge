package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

/**
 * Represents a converter for a specific Java type.
 * <p>
 * A {@code Converter} is responsible for translating an arbitrary object to the target type {@code T}.
 * It also provides information about the target type it handles.
 *
 * @param <T> the target Java type this converter handles
 */
public interface Converter<T> extends ConverterFunction<T> {

    /**
     * Returns the target Java class this converter handles.
     *
     * @return the target Java class
     */
    Class<?> type();

    /**
     * Returns the primitive counterpart of the target class, if applicable.
     *
     * @return the primitive type, or {@code null} if there is no primitive counterpart
     */
    default @Nullable Class<?> primitiveType() {
        return null;
    }
}
