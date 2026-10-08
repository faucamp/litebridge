package org.litebridge.db.spi.alias;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;

import static org.junit.jupiter.api.Assertions.*;

class AliasedColumnTest {

    @Test
    void testAliasedColumn() {
        // Given
        final Column column = new Column("TEST");
        final AliasedColumn aliasedColumn = new AliasedColumn("myAlias", column);

        // Then
        assertEquals("myAlias", aliasedColumn.alias());
        assertEquals(column, aliasedColumn.target());
    }
}