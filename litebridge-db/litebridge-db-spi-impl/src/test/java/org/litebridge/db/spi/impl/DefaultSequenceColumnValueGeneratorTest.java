package org.litebridge.db.spi.impl;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DefaultSequenceColumnValueGeneratorTest {

    @Test
    void generate() {
        // Given
        final DefaultSequenceColumnValueGenerator generator = new DefaultSequenceColumnValueGenerator("test_sequence");

        // When
        final String result = generator.generate(mock(ColumnMetaData.class));

        // Then
        assertEquals("NEXT VALUE FOR test_sequence", result);
    }
}