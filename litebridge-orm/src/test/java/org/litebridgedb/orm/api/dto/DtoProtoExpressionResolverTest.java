package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.orm.api.select.model.ProtoExpressionResolver;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.ProtoColumnExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DtoProtoExpressionResolverTest {

    @Test
    @SuppressWarnings("unchecked")
    void testResolveField() {
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final OrmTable ormTable = mock(OrmTable.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
        
        final Table table = new Table("", null, "TEST", "t1");
        when(selectSpec.getTable()).thenReturn(table);
        when(selectSpec.dtoTable()).thenReturn(ormTable);
        when(tableRegistry.getTableOrThrow(any())).thenReturn(ormTable);
        
        final ColumnMetaData col1 = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field1")).thenReturn(col1);
        final FieldAccessor field1 = mock(FieldAccessor.class);
        when(ormTable.getFieldForColumnName("COL1")).thenReturn(field1);
        when(cache.fieldAccessorOrThrow(any(), any())).thenReturn(field1);

        final DtoProtoExpressionResolver resolver = new DtoProtoExpressionResolver(selectSpec, new NoOpAliasGenerator(), cache, tableRegistry);
        
        final ProtoColumnExpressionSpec proto = new ProtoColumnExpressionSpec(SelectFieldSpec.class, "field1");
        final ExpressionSpec resolved = resolver.resolveExpression((ExpressionSpec) proto, ClauseType.SELECT).findFirst().orElseThrow();
        
        assertInstanceOf(SelectFieldSpec.class, resolved);
        assertEquals("COL1", ((SelectFieldSpec) resolved).getColumn().name());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testResolveExplicitJoinField() {
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
        
        final DtoJoinSpec joinSpec = mock(DtoJoinSpec.class);
        final OrmTable joinTable = mock(OrmTable.class);
        final Table joinSpiTable = new Table("", null, "JOIN", "t2");
        when(joinSpec.table()).thenReturn(joinSpiTable);
        when(joinSpec.dtoTable()).thenReturn(joinTable);
        when(joinSpec.dtoClass()).thenReturn((Class) String.class);
        when(selectSpec.getJoins()).thenReturn(List.of(joinSpec));
        
        final TableMetaData joinMetaData = mock(TableMetaData.class);
        when(joinTable.getMetaData()).thenReturn(joinMetaData);
        when(joinMetaData.toTable()).thenReturn(joinSpiTable);

        // Mock main table to avoid NPE in getDtoClass or getColumn
        when(selectSpec.dtoTable()).thenReturn(mock(OrmTable.class));

        final ColumnMetaData col1 = new ColumnMetaData(joinSpiTable, "COL1", true, Types.VARCHAR);
        when(joinTable.getColumnForFieldName("field1")).thenReturn(col1);
        when(tableRegistry.getTableOrThrow(String.class)).thenReturn(joinTable);
        when(cache.fieldAccessorOrThrow(any(), any())).thenReturn(mock(FieldAccessor.class));

        final DtoProtoExpressionResolver resolver = new DtoProtoExpressionResolver(selectSpec, new NoOpAliasGenerator(), cache, tableRegistry);
        
        // Use args to specify DTO class
        final ProtoColumnExpressionSpec proto = new ProtoColumnExpressionSpec(SelectFieldSpec.class, "field1", null, new Object[]{String.class});
        final ExpressionSpec resolved = resolver.resolveExpression((ExpressionSpec) proto, ClauseType.SELECT).findFirst().orElseThrow();
        
        assertInstanceOf(SelectFieldSpec.class, resolved);
        assertEquals("COL1", ((SelectFieldSpec) resolved).getColumn().name());
        assertEquals("t2", ((SelectFieldSpec) resolved).getColumn().table().alias());
    }
}
