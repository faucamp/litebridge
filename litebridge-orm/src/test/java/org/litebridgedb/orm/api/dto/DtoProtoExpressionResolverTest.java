package org.litebridgedb.orm.api.dto;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.ProtoColumnExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.sql.Types;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DtoProtoExpressionResolverTest {

    @Test
    @SuppressWarnings("unchecked")
    void testResolveQueryField() {
        // Given
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
        when(field1.name()).thenReturn("field1");
        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1);

        final DtoProtoExpressionResolver resolver = new DtoProtoExpressionResolver(selectSpec, new NoOpAliasGenerator(), cache, tableRegistry);
        
        // When
        final org.litebridgedb.orm.meta.QueryField qf = new org.litebridgedb.orm.meta.QueryField(Object.class, "field1");
        final ExpressionSpec resolved = resolver.resolveExpression(qf, ClauseType.SELECT).findFirst().orElseThrow();
        
        // Then
        assertInstanceOf(SelectFieldSpec.class, resolved);
        assertEquals("COL1", ((SelectFieldSpec) resolved).getColumn().name());
    }

    @Test
    void testGetColumnWhereClause() {
        // Given
        final DtoSelectSpec selectSpec = mock(DtoSelectSpec.class);
        final OrmTable ormTable = mock(OrmTable.class);
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
        
        final Table table = new Table("", null, "TEST", "t1");
        when(selectSpec.getTable()).thenReturn(table);
        when(selectSpec.dtoTable()).thenReturn(ormTable);
        when(selectSpec.dtoClass()).thenReturn((Class) String.class);
        when(tableRegistry.getTableOrThrow(any())).thenReturn(ormTable);
        
        final ColumnMetaData col1 = new ColumnMetaData(table, "COL1", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field1")).thenReturn(col1);
        
        final FieldAccessor field1 = mock(FieldAccessor.class);
        when(field1.name()).thenReturn("field1");
        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1);

        // Mock a selected column with alias
        final Table aliasedTable = new Table("", null, "TEST", "t1_alias");
        final org.litebridgedb.db.spi.Column selectedCol = new org.litebridgedb.db.spi.Column(aliasedTable, "COL1");
        final SelectFieldSpec sfs = mock(SelectFieldSpec.class);
        when(sfs.getColumn()).thenReturn(selectedCol);
        when(selectSpec.getExpressions()).thenReturn(List.of(sfs));

        final DtoProtoExpressionResolver resolver = new DtoProtoExpressionResolver(selectSpec, new NoOpAliasGenerator(), cache, tableRegistry);
        
        // When
        final ProtoColumnExpressionSpec proto = new ProtoColumnExpressionSpec(SelectFieldSpec.class, "field1");
        final ExpressionSpec resolved = resolver.resolveExpression((ExpressionSpec)proto, ClauseType.WHERE).findFirst().orElseThrow();
        final org.litebridgedb.db.spi.Column resultCol = ((SelectFieldSpec)resolved).getColumn();
        
        // Then
        assertEquals("COL1", resultCol.name());
        assertEquals("t1_alias", resultCol.table().alias());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testConstructorWithoutSelectSpec() {
        // Given
        final TableRegistry tableRegistry = mock(TableRegistry.class);
        final ClassFieldAccessorCache cache = mock(ClassFieldAccessorCache.class);
        final OrmTable ormTable = mock(OrmTable.class);
        when(tableRegistry.getTableOrThrow(any())).thenReturn(ormTable);
        final TableMetaData metaData = mock(TableMetaData.class);
        when(ormTable.getMetaData()).thenReturn(metaData);
        when(metaData.toTable()).thenReturn(new Table("", null, "TEST"));
        
        final ColumnMetaData col1 = new ColumnMetaData(new Table("", null, "TEST"), "COL1", true, Types.VARCHAR);
        when(ormTable.getColumnForFieldName("field1")).thenReturn(col1);
        
        final FieldAccessor field1 = mock(FieldAccessor.class);
        when(field1.name()).thenReturn("field1");
        when(cache.fieldAccessorOrThrow(any(), eq("field1"))).thenReturn(field1);

        final DtoProtoExpressionResolver resolver = new DtoProtoExpressionResolver(new NoOpAliasGenerator(), cache, tableRegistry);
        
        // When
        // Pass String.class in args[0] to specify DTO class
        final ProtoColumnExpressionSpec proto = new ProtoColumnExpressionSpec(SelectFieldSpec.class, "field1", null, new Object[]{String.class});
        final ExpressionSpec resolved = resolver.resolveExpression((ExpressionSpec)proto, ClauseType.WHERE).findFirst().orElseThrow();
        final org.litebridgedb.db.spi.Column resultCol = ((SelectFieldSpec)resolved).getColumn();
        
        // Then
        assertEquals("COL1", resultCol.name());
    }
}
