package org.litebridge.db.api.convert;

public class LongConverter {

    public Long convert(final Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Long longValue -> longValue;
            case Integer integer -> integer.longValue();
            case Short shortValue -> shortValue.longValue();
            case Byte byteValue -> byteValue.longValue();
            case Float floatValue -> floatValue.longValue();
            case Double doubleValue -> doubleValue.longValue();
            case Boolean bool -> bool ? 1L : 0L;
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        };
    }
}
