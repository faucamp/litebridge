package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RowColumnTest {

    @Test
    void constructor_3args() {
        // Given
        final Column col = new Column("name");

        // When
        final RowColumn rowColumn = new RowColumn("label", "val", col);

        // Then
        assertEquals("label", rowColumn.label());
        assertEquals("val", rowColumn.value());
        assertEquals(col, rowColumn.column());
        assertNull(rowColumn.tableAlias());
    }

    @Test
    void constructor_4args() {
        // Given
        final Column col = new Column("name");

        // When
        final RowColumn rowColumn = new RowColumn("label", "val", col, "tblAlias");

        // Then
        assertEquals("label", rowColumn.label());
        assertEquals("val", rowColumn.value());
        assertEquals(col, rowColumn.column());
        assertEquals("tblAlias", rowColumn.tableAlias());
    }

    @Test
    void structureHashCode() {
        // Given
        final Table table = new Table("T1");
        final Column colWithTable = new Column(table, "C1");
        final Column colNoTable = new Column("C1");

        final RowColumn rc1 = new RowColumn("L1", "v1", colWithTable, "A1");
        final RowColumn rc2 = new RowColumn("L1", "v2", colWithTable, "A1");
        final RowColumn rcNoAlias = new RowColumn("L1", "v1", colWithTable, null);
        final RowColumn rcNoTable = new RowColumn("L1", "v1", colNoTable, "A1");
        final RowColumn rcNullCol = new RowColumn("L1", "v1", null, "A1");

        // When & Then
        assertEquals(rc1.structureHashCode(), rc2.structureHashCode());
        assertNotEquals(rc1.structureHashCode(), rcNoAlias.structureHashCode());
        assertNotEquals(rc1.structureHashCode(), rcNoTable.structureHashCode());
        assertNotEquals(rc1.structureHashCode(), rcNullCol.structureHashCode());
    }

    @Test
    void testToString() {
        // Given
        final Column colDiff = new Column("colName");
        final Column colSame = new Column("label");

        final RowColumn rc1 = new RowColumn("label", "val", colDiff);
        final RowColumn rc2 = new RowColumn("label", "val", colSame);
        final RowColumn rc3 = new RowColumn("label", "val", null);

        // When & Then
        assertEquals("{label/colName: val}", rc1.toString());
        assertEquals("{label: val}", rc2.toString());
        assertEquals("{label: val}", rc3.toString());
    }

    @Test
    void equals_and_hashCode() {
        // Given
        final Column col = new Column("col");
        final RowColumn rc1 = new RowColumn("label", "val", col, "alias");
        final RowColumn rc2 = new RowColumn("label", "val", col, "alias");
        final RowColumn rc3 = new RowColumn("label", "diffVal", col, "alias");

        // When & Then
        assertEquals(rc1, rc1);
        assertEquals(rc1, rc2);
        assertEquals(rc1.hashCode(), rc2.hashCode());
        assertNotEquals(rc1, rc3);
        assertNotEquals(rc1, null);
    }
}
