package org.litebridge.db.spi.impl.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnType;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BindValueExpressionImplTest {

    private final LabelGenerator labelGenerator = new LabelGenerator();

    @Test
    void getters_returnConfiguredValues() {
        // Given
        final ColumnType columnType = new ColumnType(Types.INTEGER, null);
        final BindValueExpressionImpl expression = new BindValueExpressionImpl(3, 1, columnType, "valAlias", labelGenerator);

        // When / Then
        assertEquals(3, expression.index());
        assertEquals(1, expression.size());
        assertEquals(columnType, expression.columnType());
        assertEquals("valAlias", expression.alias());
    }

    @Test
    void toSql_withSingleSizeNoAlias() {
        // Given
        final ColumnType columnType = new ColumnType(Types.VARCHAR, 255);
        final BindValueExpressionImpl expression = new BindValueExpressionImpl(1, 1, columnType, null, labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = expression.toSql(select, ClauseType.WHERE, null);

        // Then
        assertEquals("?", sql);
    }

    @Test
    void toSql_withSingleSizeAndAlias() {
        // Given
        final ColumnType columnType = new ColumnType(Types.VARCHAR, 255);
        final BindValueExpressionImpl expression = new BindValueExpressionImpl(1, 1, columnType, "my_val", labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = expression.toSql(select, ClauseType.SELECT, null);

        // Then
        assertEquals("? AS \"my_val\"", sql);
    }

    @Test
    void toSql_withMultipleSize() {
        // Given
        final ColumnType columnType = new ColumnType(Types.INTEGER, null);
        final BindValueExpressionImpl expression = new BindValueExpressionImpl(1, 4, columnType, null, labelGenerator);
        final Select select = mock(Select.class);

        // When
        final String sql = expression.toSql(select, ClauseType.WHERE, null);

        // Then
        assertEquals("?, ?, ?, ?", sql);
    }

    @Test
    void equalsAndHashCode() {
        // Given
        final ColumnType type1 = new ColumnType(Types.INTEGER, null);
        final ColumnType type2 = new ColumnType(Types.VARCHAR, 255);
        final BindValueExpressionImpl expr1 = new BindValueExpressionImpl(1, 1, type1, "a", labelGenerator);
        final BindValueExpressionImpl expr2 = new BindValueExpressionImpl(1, 2, type2, "b", labelGenerator);
        final BindValueExpressionImpl expr3 = new BindValueExpressionImpl(2, 1, type1, "a", labelGenerator);

        // Then
        assertEquals(expr1, expr1);
        assertEquals(expr1, expr2); // equal because index is the same
        assertNotEquals(expr1, expr3);
        assertNotEquals(expr1, null);
        assertNotEquals(expr1, "someString");

        assertEquals(expr1.hashCode(), expr2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        final ColumnType columnType = new ColumnType(Types.INTEGER, null);
        final BindValueExpressionImpl expression = new BindValueExpressionImpl(5, 1, columnType, "alias", labelGenerator);

        // When
        final String str = expression.toString();

        // Then
        assertTrue(str.contains("BindValueExpressionImpl"));
        assertTrue(str.contains("index=5"));
    }
}
