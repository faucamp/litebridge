package org.litebridge.db.spi.impl.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CastTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();

    @Test
    void target_returnsTargetExpression() {
        // Given
        final SelectExpression target = mock(SelectExpression.class);
        final Cast cast = new Cast(target, "my_alias", Types.INTEGER, null, labelGenerator);

        // When
        final SelectExpression result = cast.target();

        // Then
        assertSame(target, result);
    }

    @Test
    void toSql_withoutSizeAndWithoutAlias() {
        // Given
        final SelectExpression target = mock(SelectExpression.class);
        final Select select = mock(Select.class);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), any())).thenReturn("col1");
        final Cast cast = new Cast(target, null, Types.INTEGER, null, labelGenerator);

        // When
        final String sql = cast.toSql(select, ClauseType.SELECT, null);

        // Then
        assertEquals("CAST(col1 AS INTEGER)", sql);
    }

    @Test
    void toSql_withSizeAndWithAlias() {
        // Given
        final SelectExpression target = mock(SelectExpression.class);
        final Select select = mock(Select.class);
        when(target.toSql(eq(select), eq(ClauseType.SELECT), any())).thenReturn("col1");
        final Cast cast = new Cast(target, "alias_name", Types.VARCHAR, 255, labelGenerator);

        // When
        final String sql = cast.toSql(select, ClauseType.SELECT, null);

        // Then
        assertEquals("CAST(col1 AS VARCHAR(255)) AS \"alias_name\"", sql);
    }
}
