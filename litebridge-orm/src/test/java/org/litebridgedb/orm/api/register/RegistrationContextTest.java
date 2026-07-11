package org.litebridgedb.orm.api.register;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.DatabaseProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class RegistrationContextTest {

    @Test
    void mapToTable() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final RegistrationContext context = new RegistrationContext(TestDto.class, databaseProvider);

        // When
        final RegistrationContextTerminal terminal = context.mapToTable("TEST_TABLE");

        // Then
        assertNotNull(terminal);
        assertEquals("TEST_TABLE", terminal.tableName);
        assertEquals(TestDto.class, terminal.dtoClass);
    }

    @Test
    void mapToTableWithInterfaces() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final RegistrationContext context = new RegistrationContext(TestDto.class, databaseProvider);

        // When
        final RegistrationContextTerminal terminal = context.allowInterface(TestInterface.class)
                .mapToTable("TEST_TABLE");

        // Then
        assertNotNull(terminal.dtoInterfaces);
        assertEquals(1, terminal.dtoInterfaces.size());
        assertEquals(TestInterface.class, terminal.dtoInterfaces.get(0));
    }

    private static class TestDto {}
    private interface TestInterface {}
}
