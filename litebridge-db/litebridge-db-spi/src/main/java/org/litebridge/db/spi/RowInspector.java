package org.litebridge.db.spi;

public final class RowInspector {

    private RowInspector() {
    }

    public static void updateColumn(final Row row, final int columnIndex, final RowColumn rowColumn) {
        row.updateColumn(columnIndex, rowColumn);
    }
}
