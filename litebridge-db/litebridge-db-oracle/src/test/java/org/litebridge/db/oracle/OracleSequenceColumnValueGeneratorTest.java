package org.litebridge.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OracleSequenceColumnValueGeneratorTest {

    @Test
    void generate() {
        // Given
        final OracleSequenceColumnValueGenerator generator = new OracleSequenceColumnValueGenerator("test_sequence");

        // When
        final String result = generator.generate(mock(ColumnMetaData.class));

        // Then
        assertEquals("test_sequence.NEXTVAL", result);
    }
}