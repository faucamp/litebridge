package org.litebridge.orm.expression.select;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SelectExpressionTest {

    @Test
    void testSelectColumnSpec() {
        // Given
        final Column column = mock(Column.class);

        // When
        final SelectColumnSpec spec = new SelectColumnSpec(column);

        // Then
        assertEquals(column, spec.getColumn());
    }
}
