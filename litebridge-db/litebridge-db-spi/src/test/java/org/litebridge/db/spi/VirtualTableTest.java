package org.litebridge.db.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualTableTest {

    @Test
    void constructor_withAlias() {
        // Given
        final VirtualTable virtualTable = new VirtualTable("vt");

        // When & Then
        assertNull(virtualTable.catalog());
        assertNull(virtualTable.schema());
        assertEquals("vt", virtualTable.name());
        assertTrue(virtualTable.isVirtual());
        assertEquals("VirtualTable[alias='vt']", virtualTable.toString());
    }

    @Test
    void anonymous() {
        // When
        final VirtualTable anonymous = VirtualTable.anonymous();

        // Then
        assertNull(anonymous.catalog());
        assertNull(anonymous.schema());
        assertEquals("", anonymous.name());
        assertTrue(anonymous.isVirtual());
    }
}
