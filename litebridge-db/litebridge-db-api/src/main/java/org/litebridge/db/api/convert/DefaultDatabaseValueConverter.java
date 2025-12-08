package org.litebridge.db.api.convert;

import java.sql.Types;

public class DefaultDatabaseValueConverter implements DatabaseValueConverter {

    private final BooleanConverter booleanConverter = new BooleanConverter();
    private final StringConverter stringConverter = new StringConverter();
    private final TimestampConverter timestampConverter = new TimestampConverter();
    private final IntegerConverter integerConverter = new IntegerConverter();
    private final LongConverter longConverter = new LongConverter();
    private final ShortConverter shortConverter = new ShortConverter();

    public Object convert(final Object value, final int dbDataType) {
        return switch (dbDataType) {
            case Types.BOOLEAN -> booleanConverter.convert(value);
            case Types.TIMESTAMP -> timestampConverter.convert(value);
            case Types.VARCHAR -> stringConverter.convert(value);
            case Types.INTEGER -> integerConverter.convert(value);
            case Types.NUMERIC,
                 Types.BIGINT -> longConverter.convert(value);
            case Types.SMALLINT -> shortConverter.convert(value);
            default -> throw new IllegalArgumentException("Unsupported database data type: " + dbDataType);
        };
    }

}
