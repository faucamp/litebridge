package org.litebridgedb.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsertResultTest {

    @Test
    void constructor_withGeneratedKeys() {
        // Given
        final int rowsAffected = 1;
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData columnMetaData = new ColumnMetaData(table, "ID", false, 1);
        final Map<ColumnMetaData, Object> generatedKeys = Map.of(columnMetaData, 123L);

        // When
        final InsertResult result = new InsertResult(rowsAffected, generatedKeys);

        // Then
        assertEquals(rowsAffected, result.rowsAffected());
        assertEquals(generatedKeys, result.generatedKeys());
    }

    @Test
    void constructor_noGeneratedKeys() {
        // Given
        final int rowsAffected = 1;

        // When
        final InsertResult result = new InsertResult(rowsAffected);

        // Then
        assertEquals(rowsAffected, result.rowsAffected());
        assertTrue(result.generatedKeys().isEmpty());
    }

    @Test
    void testToString() {
        final InsertResult result = new InsertResult(1, Collections.emptyMap());
        assertTrue(result.toString().contains("InsertResult"));
        assertTrue(result.toString().contains("generatedKeys={}"));
    }
}