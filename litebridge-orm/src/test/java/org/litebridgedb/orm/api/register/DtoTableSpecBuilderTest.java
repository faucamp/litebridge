package org.litebridgedb.orm.api.register;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.orm.api.spec.DtoTableSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class DtoTableSpecBuilderTest {

    @Test
    void build() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final RegistrationContextTerminal context = new RegistrationContextTerminal(TestDto.class, "TEST_TABLE", databaseProvider, List.of(TestInterface.class));
        context.with(builder -> builder.mapField("testField").toColumn("TEST_COLUMN"));
        final DtoTableSpecBuilder builder = new DtoTableSpecBuilder(context);

        // When
        final DtoTableSpec spec = builder.build();

        // Then
        assertNotNull(spec);
        assertEquals(TestDto.class, spec.dtoClass());
        assertEquals("TEST_TABLE", spec.tableSpec().name());
        assertEquals(1, spec.tableSpec().fieldColumnMap().size());
        assertEquals(1, spec.dtoInterfaces().size());
        assertEquals(TestInterface.class, spec.dtoInterfaces().get(0));
    }

    private static class TestDto {}
    private interface TestInterface {}
}
