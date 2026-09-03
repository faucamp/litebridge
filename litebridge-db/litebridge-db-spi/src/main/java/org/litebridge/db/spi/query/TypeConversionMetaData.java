package org.litebridge.db.spi.query;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;

import java.util.Map;
import java.util.Set;

public record TypeConversionMetaData(Map<String, ColumnMetaData> columnLabelsToColumnMetaData,
                                     Class<?>[] typeOverrides,
                                     Map<String, Table> columnAliasesToTable) {
}
