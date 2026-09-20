package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Metadata for a database table, including its primary keys and expressions.
 * <p>
 * It extends the {@link Table} class and provides additional information about the table's expressions,
 * primary key, and column mappings.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class VirtualTableMetaData extends TableMetaData {

    public VirtualTableMetaData(final VirtualTable virtualTable) {
        super(virtualTable, Collections.emptyList(), new ArrayList<>());
    }

    @Override
    public ColumnMetaData column(final String columnName) {
        ColumnMetaData columnMetaData = columnMap.get(columnName);

        if (columnMetaData == null) {
            columnMetaData = new ColumnMetaData(table, columnName, true, Types.OTHER);
            columnMap.put(columnName, columnMetaData);
            columns.add(columnMetaData);
        }

        return columnMetaData;
    }
}
