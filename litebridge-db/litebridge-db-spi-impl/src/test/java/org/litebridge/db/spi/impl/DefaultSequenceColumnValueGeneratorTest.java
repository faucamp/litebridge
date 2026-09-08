package org.litebridge.db.spi.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultSequenceColumnValueGeneratorTest {

    @Test
    void generate() {
        // Given
        final DefaultSequenceColumnValueGenerator generator = new DefaultSequenceColumnValueGenerator("test_sequence");

        // When
        final String result = generator.generate();

        // Then
        assertEquals("NEXT VALUE FOR test_sequence", result);
    }
}