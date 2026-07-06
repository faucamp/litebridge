package org.litebridgedb.db.spi.impl.function.scalar;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.DelegateExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScalarFunctionsTest {

    private final ColumnIdentifierGenerator generator = new ColumnIdentifierGenerator();
    private final Select select = mock(Select.class);

    @Test
    void abs() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Abs abs = new Abs(target, generator);

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
        final Lower lower = new Lower(target, generator);

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
        final Upper upper = new Upper(target, generator);

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
        final Substring substring = new Substring(target, 1, 5, generator);

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
        final Substring substring = new Substring(target, 2, null, generator);

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
        final Abs abs = new Abs(target, generator);
        abs.column().setAlias("my_abs");

        // When
        final String sql = abs.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("ABS(TEST.VAL) AS my_abs", sql);
    }
}
