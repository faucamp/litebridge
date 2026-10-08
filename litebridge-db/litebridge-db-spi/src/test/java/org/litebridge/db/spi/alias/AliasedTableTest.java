package org.litebridge.db.spi.alias;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AliasedTableTest {

    @Test
    void testAliasedTable() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final AliasedTable aliasedTable = new AliasedTable("myAlias", table);

        // Then
        assertEquals("myAlias", aliasedTable.alias());
        assertEquals(table, aliasedTable.target());
    }

    @Test
    void testEquals() {
        // Given
        final Table table1 = new Table("TABLE1");
        final AliasedTable aliasedTable1 = new AliasedTable("myAlias", table1);

        final Table table2 = new Table("TABLE2");
        final AliasedTable aliasedTable2 = new AliasedTable("myAlias", table2);

        final AliasedTable aliasedTable1Alt = new AliasedTable("myAlias", table1);
        final AliasedTable aliasedTable1DiffAlias = new AliasedTable("alias2", table1);

        // Then
        assertFalse(aliasedTable1.equals(aliasedTable2));
        assertFalse(aliasedTable1.equals(aliasedTable1DiffAlias));
        assertTrue(aliasedTable1.equals(aliasedTable1Alt));
    }
}