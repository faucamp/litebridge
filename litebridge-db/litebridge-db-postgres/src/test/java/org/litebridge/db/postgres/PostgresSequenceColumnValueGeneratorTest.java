package org.litebridge.db.postgres;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PostgresSequenceColumnValueGeneratorTest {

    @Test
    void generate() {
        // Given
        final PostgresSequenceColumnValueGenerator generator = new PostgresSequenceColumnValueGenerator("test_sequence");
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);

        // When
        final String result = generator.generate(columnMetaData);

        // Then
        assertEquals("nextval('test_sequence')", result);
    }
}