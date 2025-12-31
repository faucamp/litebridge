package org.litebridge.db.spi.convert;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface TypeConverter {

    @Nullable
    Object convert(@Nullable Object value, int dbDataType);

    @Nullable
    <T> T convert(@Nullable Object value, Class<T> fieldType);
}
