package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.math.MathOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class UpdateColumnTest {

    @Test
    void constructor_1arg() {
        // Given & When
        final UpdateColumn col = new UpdateColumn("C1");

        // Then
        assertEquals("C1", col.name());
        assertNull(col.generator());
        assertNull(col.mathOperator());
        assertNull(col.bindValueIndex());
    }

    @Test
    void constructor_3args() {
        // Given
        final ColumnValueGenerator gen = mock(ColumnValueGenerator.class);

        // When
        final UpdateColumn col = new UpdateColumn("C1", gen, MathOperator.ADD);

        // Then
        assertEquals("C1", col.name());
        assertEquals(gen, col.generator());
        assertEquals(MathOperator.ADD, col.mathOperator());
        assertNull(col.bindValueIndex());
    }

    @Test
    void constructor_4args_andEquals() {
        // Given
        final ColumnValueGenerator gen = mock(ColumnValueGenerator.class);
        final UpdateColumn col1 = new UpdateColumn("C1", gen, MathOperator.ADD, 0);
        final UpdateColumn col2 = new UpdateColumn("C1", gen, MathOperator.ADD, 0);
        final UpdateColumn colDiff = new UpdateColumn("C1", gen, MathOperator.ADD, 1);

        // When & Then
        assertEquals("C1", col1.name());
        assertEquals(gen, col1.generator());
        assertEquals(MathOperator.ADD, col1.mathOperator());
        assertEquals(0, col1.bindValueIndex());

        assertEquals(col1, col2);
        assertEquals(col1.hashCode(), col2.hashCode());
        assertNotEquals(col1, colDiff);
    }
}
