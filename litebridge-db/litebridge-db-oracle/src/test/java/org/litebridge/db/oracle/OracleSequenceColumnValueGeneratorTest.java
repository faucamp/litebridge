package org.litebridge.db.oracle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OracleSequenceColumnValueGeneratorTest {

    @Test
    void generate() {
        // Given
        final OracleSequenceColumnValueGenerator generator = new OracleSequenceColumnValueGenerator("test_sequence");

        // When
        final String result = generator.generate();

        // Then
        assertEquals("test_sequence.NEXTVAL", result);
    }
}