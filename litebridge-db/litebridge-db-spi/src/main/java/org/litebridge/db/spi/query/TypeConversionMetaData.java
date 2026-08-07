package org.litebridge.db.spi.query;

import org.litebridge.db.spi.ColumnMetaData;

import java.util.Map;

public record TypeConversionMetaData(Map<String, ColumnMetaData> columnLabelsToColumnMetaData,
                                     Class<?>[] typeOverrides) {
}
