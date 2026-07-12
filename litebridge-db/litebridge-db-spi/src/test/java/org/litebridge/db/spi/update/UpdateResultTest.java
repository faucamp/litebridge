package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateResultTest {

    @Test
    void constructor() {
        // Given
        final int rowsAffected = 1;

        // When
        final UpdateResult updateResult = new UpdateResult(rowsAffected);

        // Then
        assertEquals(rowsAffected, updateResult.rowsAffected());
    }

    @Test
    void testToString() {
        final UpdateResult result = new UpdateResult(5);
        assertTrue(result.toString().contains("UpdateResult"));
        assertTrue(result.toString().contains("rowsAffected=5"));
    }
}