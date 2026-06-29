package org.litebridgedb.orm.api.sql.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.math.MathOperation;
import org.litebridgedb.orm.api.update.model.UpdateSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SqlUpdateSetStepTest {

    @Test
    void to() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        UpdateSpec mockSpec = mock(UpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.to("column");

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> cv.column().equals(column) && cv.value().equals("column")));
    }

    @Test
    void increment() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        UpdateSpec mockSpec = mock(UpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.increment();

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> {
            if (!cv.column().equals(column)) return false;
            if (!(cv.value() instanceof MathOperation op)) return false;
            return op.operator() == MathOperation.Operator.ADD && op.value().equals(1);
        }));
    }

    @Test
    void add() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        UpdateSpec mockSpec = mock(UpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.add(10);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> {
            if (!cv.column().equals(column)) return false;
            if (!(cv.value() instanceof MathOperation op)) return false;
            return op.operator() == MathOperation.Operator.ADD && op.value().equals(10);
        }));
    }

    @Test
    void minus() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        UpdateSpec mockSpec = mock(UpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.minus(5);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> {
            if (!cv.column().equals(column)) return false;
            if (!(cv.value() instanceof MathOperation op)) return false;
            return op.operator() == MathOperation.Operator.SUBTRACT && op.value().equals(5);
        }));
    }

    @Test
    void multiply() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        UpdateSpec mockSpec = mock(UpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.multiply(2);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> {
            if (!cv.column().equals(column)) return false;
            if (!(cv.value() instanceof MathOperation op)) return false;
            return op.operator() == MathOperation.Operator.MULTIPLY && op.value().equals(2);
        }));
    }

    @Test
    void divide() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        UpdateSpec mockSpec = mock(UpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.divide(2);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> {
            if (!cv.column().equals(column)) return false;
            if (!(cv.value() instanceof MathOperation op)) return false;
            return op.operator() == MathOperation.Operator.DIVIDE && op.value().equals(2);
        }));
    }

    @Test
    void mod() {
        // Given
        SqlUpdater mockUpdater = mock(SqlUpdater.class);
        UpdateSpec mockSpec = mock(UpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        SqlUpdateSetStep step = new SqlUpdateSetStep(column, mockUpdater);

        // When
        SqlUpdateStep result = step.mod(3);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> {
            if (!cv.column().equals(column)) return false;
            if (!(cv.value() instanceof MathOperation op)) return false;
            return op.operator() == MathOperation.Operator.MOD && op.value().equals(3);
        }));
    }
}
