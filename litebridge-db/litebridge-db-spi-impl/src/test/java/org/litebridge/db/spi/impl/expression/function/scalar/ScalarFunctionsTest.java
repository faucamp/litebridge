package org.litebridge.db.spi.impl.expression.function.scalar;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.expression.function.scalar.Abs;
import org.litebridge.db.spi.impl.expression.function.scalar.Lower;
import org.litebridge.db.spi.impl.expression.function.scalar.Substring;
import org.litebridge.db.spi.impl.expression.function.scalar.Upper;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScalarFunctionsTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();
    private final Select select = mock(Select.class);

    @Test
    void abs() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Abs abs = new Abs(target, null, labelGenerator);

        // When
        final String sql = abs.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("ABS(TEST.VAL)", sql);
    }

    @Test
    void lower() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Lower lower = new Lower(target, null, labelGenerator);

        // When
        final String sql = lower.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("LOWER(TEST.VAL)", sql);
    }

    @Test
    void upper() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Upper upper = new Upper(target, null, labelGenerator);

        // When
        final String sql = upper.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("UPPER(TEST.VAL)", sql);
    }

    @Test
    void substring_withLength() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Substring substring = new Substring(target, 1, 5, null, labelGenerator);

        // When
        final String sql = substring.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("SUBSTRING(TEST.VAL, 1, 5)", sql);
    }

    @Test
    void substring_withoutLength() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Substring substring = new Substring(target, 2, null, null, labelGenerator);

        // When
        final String sql = substring.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("SUBSTRING(TEST.VAL, 2)", sql);
    }

    @Test
    void function_aliasPresent() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Abs abs = new Abs(target, "my_abs", labelGenerator);

        // When
        final String sql = abs.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("ABS(TEST.VAL) AS \"my_abs\"", sql);
    }
}
