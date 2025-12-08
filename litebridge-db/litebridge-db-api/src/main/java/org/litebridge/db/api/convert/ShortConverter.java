package org.litebridge.db.api.convert;

public class ShortConverter {

    public Short convert(final Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Short shortValue -> shortValue;
            case Integer integer -> integer.shortValue();
            case Long longValue -> longValue.shortValue();
            case Byte byteValue -> byteValue.shortValue();
            case Float floatValue -> floatValue.shortValue();
            case Double doubleValue -> doubleValue.shortValue();
            case Boolean bool -> bool ? (short) 1 : (short) 0;
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        };
    }
}
