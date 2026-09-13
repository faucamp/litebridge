package org.litebridge.orm.api.select.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SqlProtoExpressionResolverTest {

    private Table table;
    private SqlProtoExpressionResolver resolver;

    @BeforeEach
    void setUp() {
        table = new Table("users");
        resolver = new SqlProtoExpressionResolver();
    }

    @Test
    void resolveSelectField_withResolvable() {
        // Given
        final ProtoExpressionSpec protoExpr = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "name", "user_name");

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectField(protoExpr, null, table, ClauseType.SELECT);

        // Then
        final SelectColumnSpec selectColumnSpec = assertInstanceOf(SelectColumnSpec.class, spec);
        final Column column = selectColumnSpec.getColumn();
        assertEquals("name", column.name());
        assertEquals("user_name", column.alias());
        assertEquals(table, column.table());
    }

    @Test
    void resolveSelectField_withQueryField_throwsUnsupportedOperationException() {
        // Given
        final QueryField queryField = new QueryField(String.class, "name");

        // When & Then
        final UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> resolver.resolveSelectField(queryField, null, table, ClauseType.SELECT));
        assertEquals("QueryField not yet supported in SQL mode: " + queryField, ex.getMessage());
    }

    @Test
    void getColumn_withProtoExpressionSpecWithoutAlias() {
        // Given
        final ProtoExpressionSpec protoExpr = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "age");

        // When
        final Column column = resolver.getColumn(protoExpr, null, table, ClauseType.WHERE);

        // Then
        assertEquals("age", column.name());
        assertNull(column.alias());
        assertEquals(table, column.table());
    }

    @Test
    void getColumn_withGenericResolvable() {
        // Given
        final Resolvable resolvable = mock(Resolvable.class);
        when(resolvable.column()).thenReturn("created_at");

        // When
        final Column column = resolver.getColumn(resolvable, null, table, ClauseType.ORDER_BY);

        // Then
        assertEquals("created_at", column.name());
        assertNull(column.alias());
        assertEquals(table, column.table());
    }
}
