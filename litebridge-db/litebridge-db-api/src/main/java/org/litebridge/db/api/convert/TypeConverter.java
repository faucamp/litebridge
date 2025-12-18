package org.litebridge.db.api.convert;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface TypeConverter {

    @Nullable
    Object convert(@Nullable Object value, int dbDataType);

    @Nullable
    Object convert(@Nullable Object value, Class<?> fieldType);
}
