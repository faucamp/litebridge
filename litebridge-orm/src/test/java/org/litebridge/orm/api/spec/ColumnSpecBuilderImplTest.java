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
        final String result = columnSpecBuilder.build().name();

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void isAutoIncrement_true() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN")
                .autoIncrement().natively();

        // When
        final boolean result = columnSpecBuilder.build().isAutoIncrement();

        // Then
        assertTrue(result);
    }

    @Test
    void isAutoIncrement_false() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN");

        // When
        final boolean result = columnSpecBuilder.build().isAutoIncrement();

        // Then
        assertFalse(result);
    }

    @Test
    void sequence() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("TEST_COLUMN")
                .autoIncrement().usingSequence("TEST_SEQUENCE");

        // When
        final String result = columnSpecBuilder.build().sequence();

        // Then
        assertEquals("TEST_SEQUENCE", result);
    }

    @Test
    void sequence_notSet() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("testColumn");

        // When
        final String result = columnSpecBuilder.build().sequence();

        // Then
        assertNull(result);
    }

    @Test
    void joinOn() {
        // Given
        final ColumnSpecBuilderImpl columnSpecBuilder = new ColumnSpecBuilderImpl("testColumn")
                .joinOn("TEST_JOIN_COLUMN");

        // When
        final String result = columnSpecBuilder.build().joinColumn();

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
        assertEquals("TEST_COLUMN", columnSpecBuilder.build().joinColumn());
    }
}