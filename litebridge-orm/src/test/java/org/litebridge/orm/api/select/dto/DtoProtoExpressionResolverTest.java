package org.litebridge.orm.api.select.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.FieldAccessor;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoProtoExpressionResolverTest {

    private TableRegistry tableRegistry;
    private OrmTable ormTable;
    private Table table;
    private ColumnMetaData columnMetaData;
    private Column expectedColumn;
    private FieldAccessor fieldAccessor;
    private DtoProtoExpressionResolver resolver;

    @BeforeEach
    void setUp() {
        tableRegistry = mock(TableRegistry.class);
        ormTable = mock(OrmTable.class);
        table = new Table("test_table");
        columnMetaData = mock(ColumnMetaData.class);
        expectedColumn = new Column(table, "name");
        fieldAccessor = mock(FieldAccessor.class);

        when(columnMetaData.column()).thenReturn(expectedColumn);
        when(tableRegistry.getOrmTableOrThrow(SelectTestDto.class)).thenReturn(ormTable);
        when(ormTable.columnMetaDataForField("name")).thenReturn(columnMetaData);
        doReturnDtoClass(ormTable, SelectTestDto.class);

        resolver = new DtoProtoExpressionResolver(tableRegistry);
    }

    @Test
    void resolveSelectColumnSpec_withResolvable_protoExpressionSpecWithArgs() {
        // Given
        final ProtoExpressionSpec protoExpr = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "name", null, new Object[]{SelectTestDto.class});

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectColumnSpec(protoExpr, ormTable, table, null, ClauseType.SELECT);

        // Then
        final SelectColumnSpec selectColumnSpec = assertInstanceOf(SelectColumnSpec.class, spec);
        assertSame(expectedColumn, selectColumnSpec.getColumn());
    }

    @Test
    void resolveSelectColumnSpec_withResolvable_protoExpressionSpecEmptyArgs() {
        // Given
        final ProtoExpressionSpec protoExpr = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "name", null, new Object[0]);

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectColumnSpec(protoExpr, ormTable, table, null, ClauseType.WHERE);

        // Then
        final SelectColumnSpec selectColumnSpec = assertInstanceOf(SelectColumnSpec.class, spec);
        assertSame(expectedColumn, selectColumnSpec.getColumn());
    }

    @Test
    void resolveSelectColumnSpec_withResolvable_protoExpressionSpecDifferentType() {
        // Given
        final ProtoExpressionSpec protoExpr = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "name", null, new Object[]{SelectTestDto.class});

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectColumnSpec(protoExpr, ormTable, table, null, ClauseType.SELECT);

        // Then
        final SelectColumnSpec selectColumnSpec = assertInstanceOf(SelectColumnSpec.class, spec);
        assertSame(expectedColumn, selectColumnSpec.getColumn());
    }

    @Test
    void resolveSelectColumnSpec_withGenericResolvable() {
        // Given
        final Resolvable resolvable = mock(Resolvable.class);
        when(resolvable.column()).thenReturn("name");

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectColumnSpec(resolvable, ormTable, table, null, ClauseType.SELECT);

        // Then
        final SelectColumnSpec selectColumnSpec = assertInstanceOf(SelectColumnSpec.class, spec);
        assertSame(expectedColumn, selectColumnSpec.getColumn());
    }

    @Test
    void resolveSelectColumnSpec_withQueryField() {
        // Given
        final QueryField queryField = new QueryField(SelectTestDto.class, "name");

        // When
        final Stream<ExpressionSpec> result = resolver.resolveSelectColumnSpec(queryField, ormTable, table, null, ClauseType.SELECT);

        // Then
        final ExpressionSpec spec = result.findFirst().orElseThrow();
        final SelectColumnSpec selectColumnSpec = assertInstanceOf(SelectColumnSpec.class, spec);
        assertSame(expectedColumn, selectColumnSpec.getColumn());
    }

    @Test
    void getColumn_withResolvable() {
        // Given
        final Resolvable resolvable = mock(Resolvable.class);
        when(resolvable.column()).thenReturn("name");

        // When
        final Column column = resolver.getColumn(resolvable, ormTable, table, ClauseType.SELECT);

        // Then
        assertSame(expectedColumn, column);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void doReturnDtoClass(final OrmTable ormTable, final Class clazz) {
        when(ormTable.dtoClass()).thenReturn(clazz);
    }
}
