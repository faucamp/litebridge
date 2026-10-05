package org.litebridge.db.sqlite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SQLiteDatabaseProviderTest {

    @Test
    void getSequenceColumnValueGenerator_alwaysThrowsUnsupportedOperationException() {
        // Given
        final SQLiteDatabaseProvider provider = new SQLiteDatabaseProvider();

        // When
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> provider.sequenceColumnValueGenerator("test_sequence")
        );

        // Then
        assertEquals("SQLite does not support sequences", exception.getMessage());
    }
}
