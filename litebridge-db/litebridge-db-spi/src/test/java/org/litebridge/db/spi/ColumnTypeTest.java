package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnTypeTest {

    @Test
    void getters() {
        // Given
        final ColumnType columnType = new ColumnType(12, 255);

        // When & Then
        assertEquals(12, columnType.dataType());
        assertEquals(255, columnType.size());
    }

    @Test
    void getters_nullSize() {
        // Given
        final ColumnType columnType = new ColumnType(4, null);

        // When & Then
        assertEquals(4, columnType.dataType());
        assertNull(columnType.size());
    }

    @Test
    void equals_and_hashCode() {
        // Given
        final ColumnType type1 = new ColumnType(12, 100);
        final ColumnType type2 = new ColumnType(12, 100);
        final ColumnType typeDifferentDataType = new ColumnType(4, 100);
        final ColumnType typeDifferentSize = new ColumnType(12, 200);
        final ColumnType typeNullSize = new ColumnType(12, null);

        // When & Then
        assertEquals(type1, type1);
        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());

        assertNotEquals(type1, typeDifferentDataType);
        assertNotEquals(type1, typeDifferentSize);
        assertNotEquals(type1, typeNullSize);
        assertNotEquals(type1, null);
        assertNotEquals(type1, "not a ColumnType");
    }
}
