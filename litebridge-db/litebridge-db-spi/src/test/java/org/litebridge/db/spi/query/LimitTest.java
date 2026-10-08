package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LimitTest {

    @Test
    void constructor_bothSpecified() {
        // Given
        final Integer limit = 10;
        final Integer offset = 20;

        // When
        final Limit result = new Limit(limit, offset);

        // Then
        assertEquals(limit, result.limit());
        assertEquals(offset, result.offset());
    }

    @Test
    void constructor_limitOnly() {
        // Given & When
        final Limit result = new Limit(10, null);

        // Then
        assertEquals(10, result.limit());
        assertNull(result.offset());
    }

    @Test
    void constructor_offsetOnly() {
        // Given & When
        final Limit result = new Limit(null, 5);

        // Then
        assertNull(result.limit());
        assertEquals(5, result.offset());
    }

    @Test
    void constructor_bothNull_throwsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new Limit(null, null));
    }
}