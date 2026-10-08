package org.litebridge.orm.expression.select;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableAliasSpecTest {

    @Test
    void testTableAliasSpec() {
        // Given
        final TableAliasSpec tableAliasSpec = new TableAliasSpec("TABLE", "myAlias");

        // Then
        assertEquals("TABLE", tableAliasSpec.table());
        assertEquals("myAlias", tableAliasSpec.alias());
    }
}