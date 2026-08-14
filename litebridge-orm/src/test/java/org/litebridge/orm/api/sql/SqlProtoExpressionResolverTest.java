package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SqlProtoExpressionResolverTest {

    @Test
    void resolveSelectField() {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final Table table = new Table("TEST_TABLE");
        when(selectSpec.getTable()).thenReturn(table);
        final SqlProtoExpressionResolver resolver = new SqlProtoExpressionResolver(selectSpec);
        final Resolvable resolvable = mock(Resolvable.class);
        when(resolvable.column()).thenReturn("TEST_COLUMN");

        // When
        final ColumnExpressionSpec result = resolver.resolveSelectField(resolvable, table, ClauseType.SELECT);

        // Then
        assertInstanceOf(SelectColumnSpec.class, result);
        final Column column = ((SelectColumnSpec) result).getColumn();
        assertEquals("TEST_COLUMN", column.name());
        assertEquals(table, column.table());
    }

    @Test
    void resolveSelectFieldWithAlias() {
        // Given
        final SelectSpec selectSpec = mock(SelectSpec.class);
        final Table table = new Table("TEST_TABLE");
        when(selectSpec.getTable()).thenReturn(table);
        final SqlProtoExpressionResolver resolver = new SqlProtoExpressionResolver(selectSpec);
        final ProtoColumnExpressionSpec protoExpressionSpec = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "TEST_COLUMN", "col_alias");

        // When
        final ColumnExpressionSpec result = resolver.resolveSelectField(protoExpressionSpec, table, ClauseType.SELECT);

        // Then
        final Column column = ((SelectColumnSpec) result).getColumn();
        assertEquals("col_alias", column.alias());
    }
}
