package org.litebridge.convert.conversion;

import com.toddfast.util.convert.TypeConverter;

import java.math.BigInteger;

public class BigIntegerTypeConversion implements TypeConverter.Conversion<BigInteger> {

    public static final String TYPE_BIGINTEGER = "biginteger";

    @Override
    public Object[] getTypeKeys() {
        return new Object[]{
                BigInteger.class,
                BigInteger.class.getName(),
                TYPE_BIGINTEGER
        };
    }

    @Override
    public BigInteger convert(final Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case BigInteger bigInteger -> bigInteger;
            case Long longValue -> BigInteger.valueOf(longValue);
            case Integer intValue -> BigInteger.valueOf(intValue);
            case Short shortValue -> BigInteger.valueOf(shortValue);
            case Byte byteValue -> BigInteger.valueOf(byteValue);
            case Double doubleValue -> BigInteger.valueOf(doubleValue.longValue());
            case Float floatValue -> BigInteger.valueOf(floatValue.longValue());
            default -> new BigInteger(value.toString());
        };
    }
}
