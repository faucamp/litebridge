package org.litebridge.orm.api.select.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoProtoExpressionResolverTest {

    private AliasGenerator aliasGenerator;
    private ClassFieldAccessorCache classFieldAccessorCache;
    private TableRegistry tableRegistry;
    private OrmTable ormTable;
    private Table table;
    private ColumnMetaData columnMetaData;
    private Column expectedColumn;
    private FieldAccessor fieldAccessor;
    private DtoProtoExpressionResolver resolver;

    @BeforeEach
    void setUp() {
        aliasGenerator = new NoOpAliasGenerator();
        classFieldAccessorCache = mock(ClassFieldAccessorCache.class);
        tableRegistry = mock(TableRegistry.class);
        ormTable = mock(OrmTable.class);
        table = new Table("test_table");
        columnMetaData = mock(ColumnMetaData.class);
        expectedColumn = new Column(table, "name");
        fieldAccessor = mock(FieldAccessor.class);

        when(columnMetaData.toColumn()).thenReturn(expectedColumn);
        when(tableRegistry.getOrmTableOrThrow(SelectTestDto.class)).thenReturn(ormTable);
        when(ormTable.columnMetaDataForField("name")).thenReturn(columnMetaData);
        doReturnDtoClass(ormTable, SelectTestDto.class);

        resolver = new DtoProtoExpressionResolver(aliasGenerator, classFieldAccessorCache, tableRegistry);
    }

    @Test
    void resolveSelectField_withResolvable_protoExpressionSpecWithArgs() {
        // Given
        final ProtoExpressionSpec protoExpr = new ProtoColumnExpressionSpec(SelectFieldSpec.class, "name", null, new Object[]{SelectTestDto.class});
        when(classFieldAccessorCache.fieldAccessorOrThrow(SelectTestDto.class, "name")).thenReturn(fieldAccessor);

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectField(protoExpr, ormTable, table, ClauseType.SELECT);

        // Then
        final SelectFieldSpec fieldSpec = assertInstanceOf(SelectFieldSpec.class, spec);
        assertSame(fieldAccessor, fieldSpec.field());
        assertSame(expectedColumn, fieldSpec.getColumn());
    }

    @Test
    void resolveSelectField_withResolvable_protoExpressionSpecEmptyArgs() {
        // Given
        final ProtoExpressionSpec protoExpr = new ProtoColumnExpressionSpec(SelectFieldSpec.class, "name", null, new Object[0]);
        when(classFieldAccessorCache.fieldAccessorOrThrow(SelectTestDto.class, "name")).thenReturn(fieldAccessor);

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectField(protoExpr, ormTable, table, ClauseType.WHERE);

        // Then
        final SelectFieldSpec fieldSpec = assertInstanceOf(SelectFieldSpec.class, spec);
        assertSame(fieldAccessor, fieldSpec.field());
        assertSame(expectedColumn, fieldSpec.getColumn());
    }

    @Test
    void resolveSelectField_withResolvable_protoExpressionSpecDifferentType() {
        // Given
        final ProtoExpressionSpec protoExpr = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "name", null, new Object[]{SelectTestDto.class});
        when(classFieldAccessorCache.fieldAccessorOrThrow(SelectTestDto.class, "name")).thenReturn(fieldAccessor);

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectField(protoExpr, ormTable, table, ClauseType.SELECT);

        // Then
        final SelectFieldSpec fieldSpec = assertInstanceOf(SelectFieldSpec.class, spec);
        assertSame(fieldAccessor, fieldSpec.field());
    }

    @Test
    void resolveSelectField_withGenericResolvable() {
        // Given
        final Resolvable resolvable = mock(Resolvable.class);
        when(resolvable.column()).thenReturn("name");
        when(classFieldAccessorCache.fieldAccessorOrThrow(SelectTestDto.class, "name")).thenReturn(fieldAccessor);

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectField(resolvable, ormTable, table, ClauseType.SELECT);

        // Then
        final SelectFieldSpec fieldSpec = assertInstanceOf(SelectFieldSpec.class, spec);
        assertSame(fieldAccessor, fieldSpec.field());
    }

    @Test
    void resolveSelectField_withQueryField() {
        // Given
        final QueryField queryField = new QueryField(SelectTestDto.class, "name");
        when(classFieldAccessorCache.fieldAccessorOrThrow(SelectTestDto.class, "name")).thenReturn(fieldAccessor);

        // When
        final ColumnExpressionSpec spec = resolver.resolveSelectField(queryField, ormTable, table, ClauseType.SELECT);

        // Then
        final SelectFieldSpec fieldSpec = assertInstanceOf(SelectFieldSpec.class, spec);
        assertSame(fieldAccessor, fieldSpec.field());
        assertSame(expectedColumn, fieldSpec.getColumn());
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
