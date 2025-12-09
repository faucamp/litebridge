package org.litebridge.db.api.convert;

import jakarta.annotation.Nullable;

public interface DatabaseValueConverter {

    Object convert(@Nullable Object value, int dbDataType);
}
