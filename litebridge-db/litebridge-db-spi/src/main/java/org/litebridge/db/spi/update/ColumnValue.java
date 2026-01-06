package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;

public record ColumnValue(ColumnMetaData column, @Nullable Object value) {
}
