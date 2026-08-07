package org.litebridge.orm.api.sql.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.math.MathOperation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SqlUpdateSetStepTest {

    @Test
    void to() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.to("value");

        // Then
        assertEquals(mockUpdater, result);
        verify(mockUpdater).addSetNode(eq(column), eq("value"));
    }

    @Test
    void increment() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.increment();

        // Then
        assertEquals(mockUpdater, result);
        verify(mockUpdater).addSetNode(eq(column), argThat(v -> v instanceof MathOperation op && op.operator() == MathOperation.Operator.ADD && op.value().equals(1)));
    }

    @Test
    void add() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.add(10);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockUpdater).addSetNode(eq(column), argThat(v -> v instanceof MathOperation op && op.operator() == MathOperation.Operator.ADD && op.value().equals(10)));
    }

    @Test
    void minus() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.minus(5);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockUpdater).addSetNode(eq(column), argThat(v -> v instanceof MathOperation op && op.operator() == MathOperation.Operator.SUBTRACT && op.value().equals(5)));
    }

    @Test
    void multiply() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.multiply(2);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockUpdater).addSetNode(eq(column), argThat(v -> v instanceof MathOperation op && op.operator() == MathOperation.Operator.MULTIPLY && op.value().equals(2)));
    }

    @Test
    void divide() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.divide(2);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockUpdater).addSetNode(eq(column), argThat(v -> v instanceof MathOperation op && op.operator() == MathOperation.Operator.DIVIDE && op.value().equals(2)));
    }

    @Test
    void mod() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.mod(3);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockUpdater).addSetNode(eq(column), argThat(v -> v instanceof MathOperation op && op.operator() == MathOperation.Operator.MOD && op.value().equals(3)));
    }
}
