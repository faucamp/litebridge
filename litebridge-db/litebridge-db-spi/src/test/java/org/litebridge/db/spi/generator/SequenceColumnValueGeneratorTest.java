package org.litebridge.db.spi.generator;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequenceColumnValueGeneratorTest {

    @Test
    void generate() {
        // Given
        final SequenceColumnValueGenerator generator = new TestSequenceColumnValueGenerator("NEXT VALUE FOR test_seq");

        // When
        final String result = generator.generate();

        // Then
        assertEquals("NEXT VALUE FOR test_seq", result);
    }

    @NullMarked
    private static class TestSequenceColumnValueGenerator extends SequenceColumnValueGenerator {
        public TestSequenceColumnValueGenerator(final String sqlFragment) {
            super(sqlFragment);
        }
    }
}