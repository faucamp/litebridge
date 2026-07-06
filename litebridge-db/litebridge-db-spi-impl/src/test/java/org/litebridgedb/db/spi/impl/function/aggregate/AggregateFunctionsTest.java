package org.litebridgedb.db.spi.impl.function.aggregate;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.DelegateExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AggregateFunctionsTest {

    private final ColumnIdentifierGenerator generator = new ColumnIdentifierGenerator();
    private final Select select = mock(Select.class);

    @Test
    void avg() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Avg avg = new Avg(target, generator);

        // When
        final String sql = avg.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("AVG(TEST.VAL)", sql);
    }

    @Test
    void count() {
        // Given
        final Count count = new Count();

        // When
        final String sql = count.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("COUNT(*)", sql);
    }

    @Test
    void max() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Max max = new Max(target, generator);

        // When
        final String sql = max.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("MAX(TEST.VAL)", sql);
    }

    @Test
    void min() {
        // Given
        final Column column = new Column(new Table("TEST"), "VAL");
        final ColumnExpression target = mock(ColumnExpression.class);
        when(target.column()).thenReturn(column);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), nullable(DelegateExpression.class))).thenReturn("TEST.VAL");
        final Min min = new Min(target, generator);

        // When
        final String sql = min.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("MIN(TEST.VAL)", sql);
    }
}
