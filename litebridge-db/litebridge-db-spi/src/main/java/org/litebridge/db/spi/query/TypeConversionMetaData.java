package org.litebridge.db.spi.query;

import org.litebridge.db.spi.ColumnMetaData;

import java.util.Collections;
import java.util.Map;

public record TypeConversionMetaData(Map<String, ColumnMetaData> columnLabelsToColumnMetaData,
                                     Map<String, String> columnLabelsToTableAliases,
                                     Class<?>[] typeOverrides) {

    public TypeConversionMetaData(final Map<String, ColumnMetaData> columnLabelsToColumnMetaData,
                                  final Class<?>[] typeOverrides) {
        this(columnLabelsToColumnMetaData, Collections.emptyMap(), typeOverrides);
    }
}
