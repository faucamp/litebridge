package org.litebridge.convert.converter;

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
     * Default converter priority (10).
     */
    int DEFAULT_PRIORITY = 10;

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

    /**
     * Returns the priority of this converter.
     * <p>
     * Converters with lower priority values converters are preferred if there are data type overlaps.
     * <p>
     * As an example, if there are two converters A and B for type {@code String}, if A has a priority of 1
     * and B has a priority of 2, then A will be chosen to perform the conversion.
     *
     * @return the priority of this converter
     */
    default int priority() {
        return DEFAULT_PRIORITY;
    }
}
