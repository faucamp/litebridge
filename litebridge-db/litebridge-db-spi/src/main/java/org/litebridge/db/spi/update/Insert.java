package org.litebridge.db.spi.update;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.TableMetaData;

import java.util.List;

public record Insert(TableMetaData table, List<ColumnMetaData> columns, List<RowValue> rows)
        implements UpdateStatement {

    public Insert(final TableMetaData table, final RowValue row) {
        this(table, row.columns().stream().map(ColumnValue::column).toList(), List.of(row));
    }

    public Insert(final TableMetaData table, final List<RowValue> rows) {
        this(table, rows.get(0).columns().stream().map(ColumnValue::column).toList(), rows);
    }
}
