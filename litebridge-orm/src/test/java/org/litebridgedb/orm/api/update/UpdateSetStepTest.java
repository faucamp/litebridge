package org.litebridgedb.orm.api.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.math.MathOperation;
import org.litebridgedb.orm.api.dto.update.DtoUpdateSpec;
import org.litebridgedb.orm.api.dto.update.DtoUpdater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateSetStepTest {

    @Test
    void to() {
        // Given
        DtoUpdater<?> mockUpdater = mock(DtoUpdater.class);
        DtoUpdateSpec mockSpec = mock(DtoUpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        UpdateSetStep<DtoUpdater<?>> step = new UpdateSetStep<>(column, mockUpdater);

        // When
        DtoUpdater<?> result = step.to("column");

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> cv.column().equals(column) && cv.value().equals("column")));
    }

    @Test
    void increment() {
        // Given
        DtoUpdater<?> mockUpdater = mock(DtoUpdater.class);
        DtoUpdateSpec mockSpec = mock(DtoUpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        UpdateSetStep<DtoUpdater<?>> step = new UpdateSetStep<>(column, mockUpdater);

        // When
        DtoUpdater<?> result = step.increment();

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
        DtoUpdater<?> mockUpdater = mock(DtoUpdater.class);
        DtoUpdateSpec mockSpec = mock(DtoUpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        UpdateSetStep<DtoUpdater<?>> step = new UpdateSetStep<>(column, mockUpdater);

        // When
        DtoUpdater<?> result = step.add(10);

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
        DtoUpdater<?> mockUpdater = mock(DtoUpdater.class);
        DtoUpdateSpec mockSpec = mock(DtoUpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        UpdateSetStep<DtoUpdater<?>> step = new UpdateSetStep<>(column, mockUpdater);

        // When
        DtoUpdater<?> result = step.minus(5);

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
        DtoUpdater<?> mockUpdater = mock(DtoUpdater.class);
        DtoUpdateSpec mockSpec = mock(DtoUpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        UpdateSetStep<DtoUpdater<?>> step = new UpdateSetStep<>(column, mockUpdater);

        // When
        DtoUpdater<?> result = step.multiply(2);

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
        DtoUpdater<?> mockUpdater = mock(DtoUpdater.class);
        DtoUpdateSpec mockSpec = mock(DtoUpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        UpdateSetStep<DtoUpdater<?>> step = new UpdateSetStep<>(column, mockUpdater);

        // When
        DtoUpdater<?> result = step.divide(2);

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
        DtoUpdater<?> mockUpdater = mock(DtoUpdater.class);
        DtoUpdateSpec mockSpec = mock(DtoUpdateSpec.class);
        when(mockUpdater.updateSpec()).thenReturn(mockSpec);
        Column column = mock(Column.class);
        UpdateSetStep<DtoUpdater<?>> step = new UpdateSetStep<>(column, mockUpdater);

        // When
        DtoUpdater<?> result = step.mod(3);

        // Then
        assertEquals(mockUpdater, result);
        verify(mockSpec).addColumnValue(argThat(cv -> {
            if (!cv.column().equals(column)) return false;
            if (!(cv.value() instanceof MathOperation op)) return false;
            return op.operator() == MathOperation.Operator.MOD && op.value().equals(3);
        }));
    }
}
