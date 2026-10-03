package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LimitTest {

    @Test
    void constructor() {
        // Given
        final Integer limit = 10;
        final Integer offset = 20;

        // When
        final Limit result = new Limit(limit, offset);

        // Then
        assertEquals(limit, result.limit());
        assertEquals(offset, result.offset());
    }
}