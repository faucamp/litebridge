package org.litebridge.orm.api.spec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnSpecBuilderImplTest {

    @Test
    void name() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN");

        // When
        final String result = columnSpecBuilder.name();

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void autoIncrement_true() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN")
                .autoIncrement(true);

        // When
        final boolean result = columnSpecBuilder.autoIncrement();

        // Then
        assertTrue(result);
    }

    @Test
    void autoIncrement_false() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN")
                .autoIncrement(false);

        // When
        final boolean result = columnSpecBuilder.autoIncrement();

        // Then
        assertFalse(result);
    }

    @Test
    void autoIncrement_false_notSet() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN");

        // When
        final boolean result = columnSpecBuilder.autoIncrement();

        // Then
        assertFalse(result);
    }

    @Test
    void sequence() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN")
                .sequence("TEST_SEQUENCE");

        // When
        final String result = columnSpecBuilder.sequence();

        // Then
        assertEquals("TEST_SEQUENCE", result);
    }

    @Test
    void sequence_notSet() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("testColumn");

        // When
        final String result = columnSpecBuilder.sequence();

        // Then
        assertNull(result);
    }

    @Test
    void joinOn() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("testColumn")
                .joinOn("TEST_JOIN_COLUMN");

        // When
        final String result = columnSpecBuilder.joinColumn();

        // Then
        assertEquals("TEST_JOIN_COLUMN", result);
    }

    @Test
    void joinUsing() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN");

        // When
        columnSpecBuilder.joinUsing();

        // Then
        assertEquals("TEST_COLUMN", columnSpecBuilder.joinColumn());
    }
}