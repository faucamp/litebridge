package org.litebridge.db.api.convert;

public class IntegerConverter {

    public Integer convert(final Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Integer integer -> integer;
            case Long longValue -> longValue.intValue();
            case Short shortValue -> shortValue.intValue();
            case Byte byteValue -> byteValue.intValue();
            case Float floatValue -> floatValue.intValue();
            case Double doubleValue -> doubleValue.intValue();
            case Boolean bool -> bool ? 1 : 0;
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        };
    }
}
