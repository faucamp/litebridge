package org.litebridge.orm.expression.select;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoAliasSpecTest {

    @Test
    void testDtoAliasSpec() {
        // Given
        final DtoAliasSpec<Number> dtoAliasSpec = new DtoAliasSpec<>(Number.class, "myAlias");

        // Then
        assertEquals(Number.class, dtoAliasSpec.dtoClass());
        assertEquals("myAlias", dtoAliasSpec.alias());
    }
}