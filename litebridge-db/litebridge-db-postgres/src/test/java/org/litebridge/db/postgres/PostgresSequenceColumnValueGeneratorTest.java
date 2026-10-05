package org.litebridge.db.postgres;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresSequenceColumnValueGeneratorTest {

    @Test
    void generate() {
        // Given
        final PostgresSequenceColumnValueGenerator generator = new PostgresSequenceColumnValueGenerator("test_sequence");

        // When
        final String result = generator.generate();

        // Then
        assertEquals("nextval('test_sequence')", result);
    }
}