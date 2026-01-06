package org.litebridge.db.spi.update;

import java.util.List;

public record RowValue(List<ColumnValue> columns) {
}
