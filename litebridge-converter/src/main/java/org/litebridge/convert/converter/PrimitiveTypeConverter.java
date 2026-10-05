package org.litebridge.convert.converter;

public interface PrimitiveTypeConverter<T> {

    /**
     * Returns the primitive counterpart of the target class, if applicable.
     *
     * @return the primitive type, or {@code null} if there is no primitive counterpart
     */
    Class<?> primitiveType();
}
