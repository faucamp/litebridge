package org.litebridgedb.db.oracle.function.scalar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SubstrTest {

    private ColumnIdentifierGenerator columnIdentifierGenerator;

    @BeforeEach
    void beforeEach() {
        columnIdentifierGenerator = mock(ColumnIdentifierGenerator.class);
    }

    @Test
    void template() {
        // Given
        final Substr substr = new Substr(mock(ColumnExpression.class), 2, 5, columnIdentifierGenerator);

        // When
        final String result = substr.template();

        // Then
        assertEquals("SUBSTR(%s, 2, 5)", result);
    }

    @Test
    void template_nullLength() {
        // Given
        final Substr substr = new Substr(mock(ColumnExpression.class), 3, null, columnIdentifierGenerator);

        // When
        final String result = substr.template();

        // Then
        assertEquals("SUBSTR(%s, 3)", result);
    }
}