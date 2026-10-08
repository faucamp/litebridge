package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RowInspectorTest {

    @Test
    void updateColumn() {
        // Given
        final Column col = new Column("col");
        final List<RowColumn> columns = new ArrayList<>();
        columns.add(new RowColumn("c1", "val1", col));
        final Row row = new Row(columns);

        // When
        RowInspector.updateColumn(row, 0, new RowColumn("c1", "val2", col));

        // Then
        assertEquals("val2", row.value(0));
    }
}
