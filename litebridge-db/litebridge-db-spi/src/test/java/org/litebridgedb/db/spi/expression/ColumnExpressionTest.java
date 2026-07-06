package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.Table;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ColumnExpressionTest {

    @Test
    void column() {
        // Given
        final Column column = new Column(new Table("TABLE"), "COL");
        final ColumnExpression columnExpression = select(column);

        // When
        final Column result = columnExpression.column();

        // Then
        assertSame(column, result);
    }

    public record SelectColumnExpression(Column column) implements ColumnExpression {

        @Override
        public String toSql(final Operation operation, final ClauseType context, final @Nullable DelegateExpression parent) {
            return column.name();
        }
    }

    public static ColumnExpression select(final Column column) {
        return new SelectColumnExpression(column);
    }
}