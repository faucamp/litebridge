package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualTableMetaDataTest {

    @Test
    void column_dynamicCreationAndCaching() {
        // Given
        final VirtualTable virtualTable = new VirtualTable("vt");
        final VirtualTableMetaData metaData = new VirtualTableMetaData(virtualTable);

        // When
        final ColumnMetaData col1 = metaData.column("c1");
        final ColumnMetaData col1Again = metaData.column("c1");
        final ColumnMetaData col2 = metaData.column("c2");

        // Then
        assertSame(col1, col1Again);
        assertEquals("c1", col1.name());
        assertEquals(virtualTable, col1.table());
        assertTrue(col1.isNullable());
        assertEquals(Types.OTHER, col1.getDataType());

        assertEquals(2, metaData.columns().size());
        assertTrue(metaData.hasColumn("c1"));
        assertTrue(metaData.hasColumn("c2"));
        assertTrue(metaData.primaryKey().isEmpty());
    }
}
