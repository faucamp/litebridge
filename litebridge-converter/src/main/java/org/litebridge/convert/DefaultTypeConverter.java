package org.litebridge.convert;

import com.toddfast.util.convert.TypeConverter;
import jakarta.annotation.Nullable;
import org.litebridge.convert.conversion.SqlTimestampTypeConversion;

import java.sql.Types;

public class DefaultTypeConverter implements org.litebridge.db.api.convert.TypeConverter  {

    static {
        TypeConverter.registerTypeConversion(new SqlTimestampTypeConversion());
    }

    @Override
    public Object convert(@Nullable final Object value, final int sqlDataType) {
        return switch (sqlDataType) {
            case Types.BOOLEAN -> TypeConverter.convert(Boolean.class, value);
            case Types.TIMESTAMP -> TypeConverter.convert(java.sql.Timestamp.class, value);
            case Types.VARCHAR -> TypeConverter.convert(String.class, value);
            case Types.INTEGER -> TypeConverter.convert(Integer.class, value);
            case Types.NUMERIC,
                 Types.BIGINT -> TypeConverter.convert(Long.class, value);
            case Types.SMALLINT -> TypeConverter.convert(Short.class, value);
            default -> throw new IllegalArgumentException("Unsupported SQL data type: " + sqlDataType);
        };
    }

    @Override
    public Object convert(@Nullable final Object value, final Class<?> fieldType) {
        return TypeConverter.convert(fieldType, value);
    }
}
