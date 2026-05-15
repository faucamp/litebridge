package org.litebridgedb.db.spi.query;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LimitTest {

    @Test
    void constructor() {
        // Given
        final Optional<Integer> limit = Optional.of(10);
        final Optional<Integer> offset = Optional.of(20);

        // When
        final Limit result = new Limit(limit, offset);

        // Then
        assertEquals(limit, result.limit());
        assertEquals(offset, result.offset());
    }
}