package org.litebridge.db.spi.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ColumnExpressionTest {

    @Test
    void column() {
        // Given
        final Column column = new Column(new Table("TABLE"), "COL");
        final ColumnExpression columnExpression = new ColumnTestExpression(column);

        // When
        final Column result = columnExpression.column();

        // Then
        assertSame(column, result);
    }
}