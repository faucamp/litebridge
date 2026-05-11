package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;

public abstract class AbstractStringParsingConverter<T> implements Converter<T> {

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable T convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (type().isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        final String valueStr = value.toString();

        if (StringUtils.isBlank(valueStr)) {
            return null;
        } else {
            return convertString(valueStr);
        }
    }

    protected abstract T convertString(final String value);
}
