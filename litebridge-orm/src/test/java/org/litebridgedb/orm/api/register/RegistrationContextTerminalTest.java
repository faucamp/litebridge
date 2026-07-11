package org.litebridgedb.orm.api.register;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.orm.api.spec.ColumnMapping;
import org.litebridgedb.orm.api.spec.ColumnSpec;
import org.litebridgedb.orm.api.spec.FieldMapping;
import org.litebridgedb.orm.api.spec.FieldSpec;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationContextTerminalTest {

    @Test
    void withFieldMapping() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final RegistrationContextTerminal context = new RegistrationContextTerminal(TestDto.class, "TEST_TABLE", databaseProvider, List.of(TestInterface.class));
        
        // When
        context.with(builder -> builder.mapField("testField").toColumn("TEST_COLUMN"));

        // Then
        assertEquals(1, context.fieldColumnMap.size());
        final Map.Entry<FieldMapping, ColumnMapping> entry = context.fieldColumnMap.entrySet().iterator().next();
        assertEquals("testField", ((FieldSpec) entry.getKey()).name());
        assertEquals("TEST_COLUMN", ((ColumnSpec) entry.getValue()).name());
    }

    @Test
    void withSequencePlaceholder() {
        // Given
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final SequenceColumnValueGenerator realGenerator = mock(SequenceColumnValueGenerator.class);
        when(databaseProvider.getSequenceColumnValueGenerator("TEST_SEQ")).thenReturn(realGenerator);
        
        final RegistrationContextTerminal context = new RegistrationContextTerminal(TestDto.class, "TEST_TABLE", databaseProvider, null);
        
        // When
        context.with(builder -> builder.mapField("testField").toColumn("TEST_COLUMN").generateUsingSequence("TEST_SEQ"));

        // Then
        final ColumnMapping columnMapping = context.fieldColumnMap.values().iterator().next();
        assertTrue(columnMapping instanceof ColumnSpec);
        assertEquals(realGenerator, ((ColumnSpec) columnMapping).generator());
    }

    private static class TestDto {}
    private interface TestInterface {}
}
