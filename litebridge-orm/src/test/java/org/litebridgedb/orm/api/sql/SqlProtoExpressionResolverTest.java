package org.litebridgedb.orm.api.sql;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.orm.api.select.model.SelectSpec;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ProtoColumnExpressionSpec;
import org.litebridgedb.orm.expression.Resolvable;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        final ColumnExpressionSpec result = resolver.resolveSelectField(resolvable, ClauseType.SELECT);

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
        final ColumnExpressionSpec result = resolver.resolveSelectField(protoExpressionSpec, ClauseType.SELECT);

        // Then
        final Column column = ((SelectColumnSpec) result).getColumn();
        assertEquals("col_alias", column.alias());
    }

    @Test
    void resolveSelectFieldThrowsWhenSelectSpecNotSet() {
        // Given
        final SqlProtoExpressionResolver resolver = new SqlProtoExpressionResolver();
        final Resolvable resolvable = mock(Resolvable.class);

        // When / Then
        assertThrows(NullPointerException.class, () -> resolver.resolveSelectField(resolvable, ClauseType.SELECT));
    }
}
