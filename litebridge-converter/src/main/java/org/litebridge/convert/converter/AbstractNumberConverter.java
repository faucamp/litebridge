package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;

public abstract class AbstractNumberConverter<T extends Number> implements Converter<T> {

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable T convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (type() == value.getClass()) {
            return (T) value;
        } else if (value instanceof Number number) {
            return convertNumber(number);
        }

        final String valueStr = value.toString();

        if (StringUtils.isBlank(valueStr)) {
            return null;
        } else {
            return convertString(valueStr);
        }
    }

    protected abstract T convertNumber(final Number value);

    protected abstract T convertString(final String value);
}
