package org.litebridge.db.spi.generator;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SequenceColumnValueGeneratorTest {

    @Test
    void generate() {
        // Given
        final SequenceColumnValueGenerator generator = new TestSequenceColumnValueGenerator("test_sequence");

        // When
        final String result = generator.generate(mock(ColumnMetaData.class));

        // Then
        assertEquals("TEST", result);
    }

    @NullMarked
    private static class TestSequenceColumnValueGenerator extends SequenceColumnValueGenerator {
        public TestSequenceColumnValueGenerator(String sequenceName) {
            super(sequenceName);
        }

        @Override
        public String generate(final ColumnMetaData columnMetaData) {
            return "TEST";
        }
    }
}