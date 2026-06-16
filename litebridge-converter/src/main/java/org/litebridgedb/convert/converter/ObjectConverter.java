package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

/**
 * A converter for returning {@link Object} values.
 * <p>
 * This "converter" simply returns the input value and doesn't perform any conversion.
 */
public class ObjectConverter implements Converter<Object> {

    @Override
    public Class<?> type() {
        return Object.class;
    }

    @Override
    public @Nullable Object convert(final @Nullable Object value) {
        return value;
    }
}
