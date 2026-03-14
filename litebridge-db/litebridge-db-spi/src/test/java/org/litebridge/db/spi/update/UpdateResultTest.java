package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}