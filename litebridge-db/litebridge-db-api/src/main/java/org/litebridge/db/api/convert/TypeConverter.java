package org.litebridge.db.api.convert;

import jakarta.annotation.Nullable;

public interface TypeConverter {

    Object convert(@Nullable Object value, int dbDataType);

    Object convert(@Nullable Object value, Class<?> fieldType);
}
