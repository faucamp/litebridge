package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InsertResultTest {

    @Test
    void constructor_withGeneratedKeys() {
        // Given
        final int rowsAffected = 1;
        final List<Object> generatedKeys = List.of(123L);

        // When
        final InsertResult result = new InsertResult(rowsAffected, generatedKeys);

        // Then
        assertEquals(rowsAffected, result.rowsAffected());
        assertEquals(generatedKeys, result.generatedKeys());
    }

    @Test
    void constructor_noGeneratedKeys() {
        // Given
        final int rowsAffected = 1;

        // When
        final InsertResult result = new InsertResult(rowsAffected);

        // Then
        assertEquals(rowsAffected, result.rowsAffected());
        assertTrue(result.generatedKeys().isEmpty());
    }
}